package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yinhai.mids.business.constant.AttachmentType;
import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.AttachmentPO;
import com.yinhai.mids.business.entity.po.FavoritePO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.entity.po.StudyPO;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.mapper.AttachmentMapper;
import com.yinhai.mids.business.mapper.FavoriteMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.mapper.StudyMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.StudyService;
import com.yinhai.mids.business.util.DicomUtil;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.mids.common.util.SecurityKit;
import com.yinhai.ta404.core.event.EventPublish;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

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
    private AttachmentMapper attachmentMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private EventPublish eventPublish;

    @Override
    public void uploadDicom(MultipartFile dicomZip) throws IOException {
        AppAssert.equals("zip", FileTypeUtil.getType(dicomZip.getInputStream()), "只允许上传dicom zip文件");

        // 解压并读取dicom信息
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("dicom").toFile();
        } catch (IOException e) {
            throw new AppException("创建临时文件异常");
        }
        Map<String, DicomInfo> studyMap = new HashMap<>();
        Multimap<String, DicomInfo> seriesMap = ArrayListMultimap.create();
        try (InputStream inputStream = dicomZip.getInputStream()) {
            ZipUtil.unzip(inputStream, tempDir, Charset.defaultCharset());
            for (File dicomFile : FileUtil.ls(tempDir.getAbsolutePath())) {
                DicomInfo dicomInfo = DicomUtil.readDicomInfo(dicomFile);
                if (!studyMap.containsKey(dicomInfo.getStudyUid())) {
                    studyMap.put(dicomInfo.getStudyUid(), dicomInfo);
                }
                seriesMap.put(dicomInfo.getSeriesUid(), dicomInfo);
            }
        }

        // 保存study
        Map<String, StudyPO> studyPOMap = new HashMap<>();
        List<StudyPO> studyPOList = studyMap.values().stream().map(dicomInfo -> {
            StudyPO studyPO = new StudyPO();
            BeanUtil.copyProperties(dicomInfo, studyPO);
            studyPO.setAlgorithmType("1");
            studyPO.setPrintStatus("1");
            studyPO.setPushStatus("1");
            studyPO.setUploadTime(MapperKit.executeForDate());
            studyPO.setUploadUserId(SecurityKit.currentUserId());
            studyPOMap.put(dicomInfo.getStudyUid(), studyPO);
            return studyPO;
        }).collect(toList());
        studyMapper.insertBatch(studyPOList);

        // 保存series
        seriesMapper.insertBatch(seriesMap.keySet().stream().map(seriesUid -> {
            DicomInfo dicomInfo = CollUtil.getFirst(seriesMap.get(seriesUid));
            SeriesPO seriesPO = new SeriesPO();
            seriesPO.setStudyId(studyPOMap.get(dicomInfo.getStudyUid()).getId());
            BeanUtil.copyProperties(dicomInfo, seriesPO);
            seriesPO.setImageCount(seriesMap.get(seriesUid).size());
            seriesPO.setAlgorithmType("1");
            seriesPO.setComputeStatus("1");
            seriesPO.setOperateStatus("1");
            return seriesPO;
        }).collect(toList()));

        // 保存上传文件
        UploadResult uploadResult = fileStoreService.upload(dicomZip);
        attachmentMapper.insertBatch(studyPOList.stream().map(studyPO -> {
            AttachmentPO attachmentPO = new AttachmentPO();
            attachmentPO.setStoreId(uploadResult.getStoreId());
            attachmentPO.setObjectId(studyPO.getId());
            attachmentPO.setUseType(AttachmentType.DICOM_UPLOAD_ZIP);
            return attachmentPO;
        }).collect(toList()));

        // 异步保存instances
        Map<String, Object> eventSource = new HashMap<>();
        eventSource.put("studyIdList", studyPOList.stream().map(StudyPO::getId).collect(toList()));
        eventSource.put("seriesMap", seriesMap);
        eventPublish.publish(eventSource, EventConstants.DICOM_UPLOAD);
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
