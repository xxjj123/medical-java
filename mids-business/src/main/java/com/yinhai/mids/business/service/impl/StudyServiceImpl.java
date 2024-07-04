package com.yinhai.mids.business.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.AttachmentType;
import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.DicomInstance;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.StudyService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.mids.common.util.SecurityKit;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuhs
 * @date 2024/7/2 9:16
 */
@Slf4j
@Service
@TaTransactional
public class StudyServiceImpl implements StudyService {

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private AttachmentMapper attachmentMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void uploadDicom(MultipartFile dicom) throws IOException {
        log.debug("接收到文件");
        String type = FileTypeUtil.getType(dicom.getInputStream());
        AppAssert.isTrue("zip".equals(type), "当前只允许上传dicom zip文件");
        List<DicomInstance> dicomInstanceList = new ArrayList<>();
        try (InputStream is = dicom.getInputStream()) {
            File tempDir = Files.createTempDirectory("dicom").toFile();
            ZipUtil.unzip(is, tempDir, Charset.defaultCharset());
            for (File f : FileUtil.ls(tempDir.getAbsolutePath())) {
                dicomInstanceList.add(readDicomInfo(f));
            }
        }

        List<String> studyIdList = saveStudies(dicomInstanceList);
        saveSeries(dicomInstanceList);
        instanceMapper.insertBatch(dicomInstanceList.stream().map(e -> ((InstancePO) e)).collect(Collectors.toList()));
        saveZipFile(dicom, studyIdList);
        saveDicomFiles(dicomInstanceList);
        log.debug("完成返回");
    }

    private List<String> saveStudies(List<DicomInstance> dicomInstanceList) {
        List<String> studyIdList = new ArrayList<>();
        Map<String, List<DicomInstance>> studyGroup = dicomInstanceList.stream().collect(Collectors.groupingBy(DicomInstance::getStudyUid));
        for (Map.Entry<String, List<DicomInstance>> entry : studyGroup.entrySet()) {
            DicomInstance instance = entry.getValue().get(0);
            StudyPO studyPO = new StudyPO();
            studyPO.setStudyUid(instance.getStudyUid());
            studyPO.setAccessionNumber(instance.getAccessionNumber());
            studyPO.setPatientId(instance.getPatientId());
            studyPO.setPatientName(instance.getPatientName());
            studyPO.setPatientAge(instance.getPatientAge());
            studyPO.setStudyDatetime(instance.getStudyDatetime());
            studyPO.setStudyDescription(instance.getStudyDescription());
            studyPO.setAlgorithmType("1");
            studyPO.setPrintStatus("1");
            studyPO.setPushStatus("1");
            studyPO.setUploadTime(MapperKit.executeForDate());
            studyPO.setUploadUserId(SecurityKit.currentUserId());
            studyMapper.insert(studyPO);
            entry.getValue().forEach(e -> e.setStudyId(studyPO.getId()));
            studyIdList.add(studyPO.getId());
        }
        return studyIdList;
    }

    private void saveSeries(List<DicomInstance> dicomInstanceList) {
        Map<String, List<DicomInstance>> seriesGroup = dicomInstanceList.stream().collect(Collectors.groupingBy(DicomInstance::getSeriesUid));
        for (Map.Entry<String, List<DicomInstance>> entry : seriesGroup.entrySet()) {
            DicomInstance instance = entry.getValue().get(0);
            SeriesPO seriesPO = new SeriesPO();
            seriesPO.setStudyId(instance.getStudyId());
            seriesPO.setStudyUid(instance.getStudyUid());
            seriesPO.setSeriesUid(instance.getSeriesUid());
            seriesPO.setSeriesDescription(instance.getSeriesDescription());
            seriesPO.setImageCount(entry.getValue().size());
            seriesPO.setAlgorithmType("1");
            seriesPO.setComputeStatus("1");
            seriesPO.setOperateStatus("1");
            seriesMapper.insert(seriesPO);
            entry.getValue().forEach(e -> e.setSeriesId(seriesPO.getId()));
        }
    }

    private void saveZipFile(MultipartFile dicom, List<String> studyIdList) throws IOException {
        UploadResult uploadResult = fileStoreService.upload(dicom);
        attachmentMapper.insertBatch(studyIdList.stream().map(studyId -> {
            AttachmentPO attachmentPO = new AttachmentPO();
            attachmentPO.setStoreId(uploadResult.getStoreId());
            attachmentPO.setObjectId(studyId);
            attachmentPO.setUseType(AttachmentType.DICOM_ZIP);
            return attachmentPO;
        }).collect(Collectors.toList()));
    }

    private void saveDicomFiles(List<DicomInstance> dicomInstanceList) {
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        for (DicomInstance instance : dicomInstanceList) {
            ContextFSObject<String> contextFSObject = new ContextFSObject<>();
            contextFSObject.setContext(instance.getId());
            contextFSObject.setName(instance.getInstanceFile().getName());
            contextFSObject.setInputstream(FileUtil.getInputStream(instance.getInstanceFile()));
            contextFSObject.setContentType(FileUtil.getMimeType(instance.getInstanceFile().getAbsolutePath()));
            contextFSObject.setSize(FileUtil.size(instance.getInstanceFile()));
            contextFSObjects.add(contextFSObject);
        }
        List<ContextUploadResult<String>> contextUploadResults = fileStoreService.upload(contextFSObjects);
        attachmentMapper.insertBatch(contextUploadResults.stream().map(i -> {
            AttachmentPO attachmentPO = new AttachmentPO();
            attachmentPO.setStoreId(i.getStoreId());
            attachmentPO.setObjectId(i.getContext());
            attachmentPO.setUseType(AttachmentType.DICOM_FILE);
            return attachmentPO;
        }).collect(Collectors.toList()));
    }

    private DicomInstance readDicomInfo(File dicomFile) {
        DicomInstance info = new DicomInstance();
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            Attributes dataset = dis.readDataset();
            info.setStudyUid(dataset.getString(Tag.StudyInstanceUID));
            info.setAccessionNumber(dataset.getString(Tag.AccessionNumber));
            info.setPatientId(dataset.getString(Tag.PatientID));
            info.setPatientName(dataset.getString(Tag.PatientName));
            info.setPatientAge(dataset.getString(Tag.PatientAge));
            info.setStudyDatetime(dataset.getDate(Tag.StudyDateAndTime));
            info.setStudyDescription(dataset.getString(Tag.StudyDescription));
            info.setSeriesUid(dataset.getString(Tag.SeriesInstanceUID));
            info.setSeriesDescription(dataset.getString(Tag.SeriesDescription));
            info.setInstanceUid(dataset.getString(Tag.SOPInstanceUID));
            info.setInstanceNumber(Integer.valueOf(dataset.getString(Tag.InstanceNumber)));
            info.setInstanceFile(dicomFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return info;
    }

    @Override
    public Page<StudyPageVO> pageStudies(StudyPageQuery studyPageQuery, PageRequest pageRequest) {
        PageKit.startPage(pageRequest);
        List<StudyPageVO> studyPageVOList = studyMapper.list(studyPageQuery, SecurityKit.currentUserId());
        for (StudyPageVO studyPageVO : studyPageVOList) {
            studyPageVO.setSeriesCount(studyPageVO.getSeriesList().size());
        }
        return PageKit.finishPage(studyPageVOList);
    }

    @Override
    public void addFavorite(String studyId) {
        boolean studyExists = studyMapper.exists(Wrappers.<StudyPO>lambdaQuery().eq(StudyPO::getId, studyId));
        AppAssert.isTrue(studyExists, "该检查不存在！");
        String currentUserId = SecurityKit.currentUserId();
        boolean favoriteExists = favoriteMapper.exists(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isFalse(favoriteExists, "该检查已经收藏，无需重复收藏");
        FavoritePO favoritePO = new FavoritePO();
        favoritePO.setUserId(currentUserId);
        favoritePO.setStudyId(studyId);
        int inserted = favoriteMapper.insert(favoritePO);
        AppAssert.isTrue(inserted == 1, "收藏失败");
    }

    @Override
    public void removeFavorite(String studyId) {
        boolean studyExists = studyMapper.exists(Wrappers.<StudyPO>lambdaQuery().eq(StudyPO::getId, studyId));
        AppAssert.isTrue(studyExists, "该检查不存在！");
        String currentUserId = SecurityKit.currentUserId();
        boolean favoriteExists = favoriteMapper.exists(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(favoriteExists, "该检查没有被收藏，不需要取消收藏");
        int deleted = favoriteMapper.delete(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(deleted > 0, "取消收藏失败");
    }

    @Override
    public void deleteStudy(String studyId) {
        boolean studyExists = studyMapper.exists(Wrappers.<StudyPO>lambdaQuery().eq(StudyPO::getId, studyId));
        AppAssert.isTrue(studyExists, "该检查不存在！");
        int deleted = studyMapper.deleteById(studyId);
        AppAssert.isTrue(deleted == 1, "删除检查失败");
    }

    @Override
    public void deleteSeries(String seriesId) {
        boolean seriesExists = seriesMapper.exists(Wrappers.<SeriesPO>lambdaQuery().eq(SeriesPO::getId, seriesId));
        AppAssert.isTrue(seriesExists, "该序列不存在！");
        int deleted = seriesMapper.deleteById(seriesId);
        AppAssert.isTrue(deleted == 1, "删除序列失败");
    }
}
