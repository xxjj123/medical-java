package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.PrintStatus;
import com.yinhai.mids.business.constant.PushStatus;
import com.yinhai.mids.business.entity.dto.AlgorithmParam;
import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.event.TxEventPublisher;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.StudyService;
import com.yinhai.mids.business.util.DicomUtil;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.mids.common.util.SecurityKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
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

/**
 * @author zhuhs
 * @date 2024/7/2 9:16
 */
@Service
@TaTransactional
public class StudyServiceImpl implements StudyService {

    private static final Log log = LogFactory.get();

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private SeriesComputeMapper seriesComputeMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private TxEventPublisher eventPublisher;

    @Override
    public void uploadDicom(MultipartFile dicomZip, List<AlgorithmParam> algorithmParamList) throws IOException {
        try (InputStream inputStream = dicomZip.getInputStream()) {
            AppAssert.equals("zip", FileTypeUtil.getType(inputStream), "只允许上传dicom zip文件");
        }

        File unzippedDicomDir = unzipDicomZip(dicomZip);
        try {
            List<DicomInfo> dicomInfoList = DicomUtil.readDicomInfoFromDir(unzippedDicomDir);
            AppAssert.notEmpty(dicomInfoList, "未读取到dicom文件信息");
            List<StudyPO> studyPOList = saveStudy(dicomInfoList);
            List<SeriesPO> seriesPOList = saveSeries(dicomInfoList, studyPOList);
            saveInstance(dicomInfoList, seriesPOList);
            List<SeriesComputePO> seriesComputePOList = saveSeriesCompute(seriesPOList, algorithmParamList);
            seriesComputePOList.forEach(e -> eventPublisher.publish(e.getId(), EventConstants.COMPUTE_EVENT));
        } finally {
            FileUtil.del(unzippedDicomDir);
        }
    }

    /**
     * 保存检查
     *
     * @param dicomInfoList DicomInfo列表
     * @return {@link List }<{@link StudyPO }>
     * @author zhuhs 2024/07/11 10:13
     */
    private List<StudyPO> saveStudy(List<DicomInfo> dicomInfoList) {
        List<StudyPO> studyPOList = new ArrayList<>();
        for (DicomInfo dicomInfo : CollUtil.distinct(dicomInfoList, DicomInfo::getSeriesInstanceUid, false)) {
            StudyPO studyPO = new StudyPO();
            BeanUtil.copyProperties(dicomInfo, studyPO);
            studyPO.setPrintStatus(PrintStatus.NOT_PRINT);
            studyPO.setPushStatus(PushStatus.NOT_PUSH);
            studyPO.setUploadTime(MapperKit.executeForDate());
            studyPO.setUploadUserId(SecurityKit.currentUserId());
            studyPOList.add(studyPO);
        }
        if (CollUtil.size(studyPOList) == 1) {
            studyMapper.insert(studyPOList.get(0));
        } else {
            studyMapper.insertBatch(studyPOList);
        }
        return studyPOList;
    }

    /**
     * 保存序列
     *
     * @param dicomInfoList DicomInfo列表
     * @param studyPOList   检查列表
     * @return {@link List }<{@link SeriesPO }>
     * @author zhuhs 2024/07/11 10:18
     */
    private List<SeriesPO> saveSeries(List<DicomInfo> dicomInfoList, List<StudyPO> studyPOList) {
        List<SeriesPO> seriesPOList = new ArrayList<>();
        for (DicomInfo dicomInfo : CollUtil.distinct(dicomInfoList, DicomInfo::getSeriesInstanceUid, false)) {
            SeriesPO seriesPO = new SeriesPO();
            BeanUtil.copyProperties(dicomInfo, seriesPO);
            seriesPO.setStudyId(CollUtil.findOne(studyPOList,
                    e -> StrUtil.equals(e.getStudyInstanceUid(), dicomInfo.getStudyInstanceUid())).getId());
            seriesPO.setImageCount(CollUtil.count(dicomInfoList,
                    e -> StrUtil.equals(e.getSeriesInstanceUid(), dicomInfo.getSeriesInstanceUid())));
            seriesPOList.add(seriesPO);
        }
        if (CollUtil.size(seriesPOList) == 1) {
            seriesMapper.insert(seriesPOList.get(0));
        } else {
            seriesMapper.insertBatch(seriesPOList);
        }
        return seriesPOList;
    }

    /**
     * 保存实例
     *
     * @param dicomInfoList DicomInfo列表
     * @param seriesPOList  序列列表
     * @author zhuhs 2024/07/11 10:29
     */
    private void saveInstance(List<DicomInfo> dicomInfoList, List<SeriesPO> seriesPOList) {
        List<InstancePO> instancePOList = new ArrayList<>();
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        for (DicomInfo dicomInfo : dicomInfoList) {
            InstancePO instancePO = new InstancePO();
            BeanUtil.copyProperties(dicomInfo, instancePO);
            SeriesPO seriesPO = CollUtil.findOne(seriesPOList,
                    e -> StrUtil.equals(e.getSeriesInstanceUid(), dicomInfo.getSeriesInstanceUid()));
            instancePO.setStudyId(seriesPO.getStudyId());
            instancePO.setSeriesId(seriesPO.getId());
            instancePOList.add(instancePO);
            ContextFSObject<String> contextFSObject;
            try {
                contextFSObject = new ContextFSObject<>(dicomInfo.getFile());
            } catch (IOException e) {
                log.error(e);
                throw new AppException("保存dicom文件异常");
            }
            contextFSObject.setContext(dicomInfo.getSopInstanceUid());
            contextFSObjects.add(contextFSObject);
        }
        List<ContextUploadResult<String>> contextUploadResults = fileStoreService.upload(contextFSObjects);
        for (InstancePO instancePO : instancePOList) {
            instancePO.setAccessPath(CollUtil.findOne(contextUploadResults,
                    e -> StrUtil.equals(e.getContext(), instancePO.getSopInstanceUid())).getAccessPath());
        }
        instanceMapper.insertBatch(instancePOList);
    }

    /**
     * 保存计算序列
     *
     * @param seriesPOList       序列列表
     * @param algorithmParamList 算法配置
     * @return {@link List }<{@link SeriesComputePO }>
     * @author zhuhs 2024/07/11 10:56
     */
    private List<SeriesComputePO> saveSeriesCompute(List<SeriesPO> seriesPOList, List<AlgorithmParam> algorithmParamList) {
        List<SeriesComputePO> seriesComputePOList = new ArrayList<>();

        if (CollUtil.isEmpty(algorithmParamList) || CollUtil.isEmpty(algorithmParamList.get(0).getAlgorithmTypeList())) {
            for (SeriesPO seriesPO : seriesPOList) {
                SeriesComputePO seriesComputePO = new SeriesComputePO();
                seriesComputePO.setStudyId(seriesPO.getStudyId());
                seriesComputePO.setSeriesId(seriesPO.getId());
                seriesComputePO.setComputeStatus(ComputeStatus.WAIT_COMPUTE);
                seriesComputePOList.add(seriesComputePO);
            }
            seriesComputeMapper.insertBatch(seriesComputePOList);
            return seriesComputePOList;
        }

        for (SeriesPO seriesPO : seriesPOList) {
            for (AlgorithmParam algorithmParam : algorithmParamList) {
                if (!StrUtil.equals(algorithmParam.getSeriesInstanceUid(), seriesPO.getSeriesInstanceUid())) {
                    continue;
                }
                for (String algorithmType : algorithmParam.getAlgorithmTypeList()) {
                    SeriesComputePO seriesComputePO = new SeriesComputePO();
                    seriesComputePO.setStudyId(seriesPO.getStudyId());
                    seriesComputePO.setSeriesId(seriesPO.getId());
                    seriesComputePO.setAlgorithmType(algorithmType);
                    seriesComputePO.setApplyId(IdUtil.fastSimpleUUID());
                    seriesComputePO.setComputeStatus(ComputeStatus.WAIT_COMPUTE);
                    seriesComputePOList.add(seriesComputePO);
                }
            }
        }
        seriesComputeMapper.insertBatch(seriesComputePOList);
        return seriesComputePOList;
    }

    /**
     * 创建临时文件夹用于解压dicom zip文件并返回该临时文件夹
     *
     * @param dicomZip dicom zip文件
     * @return {@link File }
     * @author zhuhs 2024/07/11 09:45
     */
    private File unzipDicomZip(MultipartFile dicomZip) {
        // 创建临时文件用于解压DICOM ZIP文件
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("dicom").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }

        try (InputStream inputStream = dicomZip.getInputStream()) {
            ZipUtil.unzip(inputStream, tempDir, Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e);
            throw new AppException("读取DICOM文件内容异常");
        }
        return tempDir;
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
    public void deleteSeriesCompute(String seriesComputeId) {
        boolean seriesExists = seriesComputeMapper.exists(
                Wrappers.<SeriesComputePO>lambdaQuery().eq(SeriesComputePO::getId, seriesComputeId));
        AppAssert.isTrue(seriesExists, "该序列不存在！");
        int deleted = seriesComputeMapper.deleteById(seriesComputeId);
        AppAssert.isTrue(deleted == 1, "删除序列失败");
    }

}
