package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
import com.yinhai.mids.business.constant.MprType;
import com.yinhai.mids.business.entity.dto.CaseStudyQuery;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.model.DicomInstance;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.CaseSeriesVO;
import com.yinhai.mids.business.entity.vo.CaseStudyVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.CaseService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.util.DicomUtil;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbClock;
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
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

/**
 * @author zhuhs
 * @date 2024/10/14
 */
@Service
@TaTransactional
public class CaseServiceImpl implements CaseService {

    private static final Log log = LogFactory.get();

    @Resource
    private CaseMapper caseMapper;

    @Resource
    private StudyInfoMapper studyInfoMapper;

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

    @Resource
    private InstanceInfoMapper instanceInfoMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private KeyaApplyTaskMapper keyaApplyTaskMapper;

    @Resource
    private KeyaQueryTaskMapper keyaQueryTaskMapper;

    @Resource
    private MprTaskMapper mprTaskMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void createCase(MultipartFile caseFile) throws IOException {
        AppAssert.isTrue(DicomUtil.isZip(caseFile.getInputStream()), "只允许上传dicom zip压缩包");

        File caseFileTempDir = createCaseFileTempDir(caseFile.getInputStream());
        try {
            List<DicomInfo> dicomInfoList = DicomUtil.readDicomInfoFromDir(caseFileTempDir);
            AppAssert.notEmpty(dicomInfoList, "未读取到dicom文件信息");
            CasePO casePO = saveCase();
            List<DicomInstance> dicomInstanceList = BeanUtil.copyToList(dicomInfoList, DicomInstance.class);
            dicomInstanceList.forEach(e -> e.setCaseId(casePO.getCaseId()));
            saveStudyInfo(dicomInstanceList);
            saveSeriesInfo(dicomInstanceList);
            saveInstanceInfo(dicomInstanceList);
            createSubTask(createComputeSeries(dicomInstanceList));
        } finally {
            FileUtil.del(caseFileTempDir);
        }
    }

    /**
     * 保存案例
     */
    private CasePO saveCase() {
        CasePO casePO = new CasePO();
        casePO.setCaseCreateTime(DbClock.now());
        casePO.setCaseCreateUserId(SecurityKit.currentUserId());
        caseMapper.insert(casePO);
        return casePO;
    }

    /**
     * 保存检查
     */
    private void saveStudyInfo(List<DicomInstance> dicomInstanceList) {
        List<StudyInfoPO> studyInfoList = new ArrayList<>();
        Map<String, List<DicomInstance>> group = dicomInstanceList
                .stream().collect(groupingBy(DicomInstance::getStudyInstanceUid));
        for (String key : group.keySet()) {
            studyInfoList.add(BeanUtil.copyProperties(group.get(key).get(0), StudyInfoPO.class));
        }
        if (CollUtil.size(studyInfoList) == 1) {
            studyInfoMapper.insert(studyInfoList.get(0));
        } else {
            studyInfoMapper.insertBatch(studyInfoList);
        }
        for (StudyInfoPO studyInfo : studyInfoList) {
            group.get(studyInfo.getStudyInstanceUid()).forEach(e -> e.setStudyId(studyInfo.getStudyId()));
        }
    }

    /**
     * 保存序列
     */
    private void saveSeriesInfo(List<DicomInstance> dicomInstanceList) {
        List<SeriesInfoPO> seriesInfoList = new ArrayList<>();
        Map<String, List<DicomInstance>> group = dicomInstanceList
                .stream().collect(groupingBy(DicomInstance::getSeriesInstanceUid));
        for (String key : group.keySet()) {
            SeriesInfoPO seriesInfoPO = BeanUtil.copyProperties(group.get(key).get(0), SeriesInfoPO.class);
            seriesInfoPO.setImageCount(group.get(key).size());
            seriesInfoList.add(seriesInfoPO);
        }
        if (CollUtil.size(seriesInfoList) == 1) {
            seriesInfoMapper.insert(seriesInfoList.get(0));
        } else {
            seriesInfoMapper.insertBatch(seriesInfoList);
        }
        for (SeriesInfoPO seriesInfo : seriesInfoList) {
            group.get(seriesInfo.getSeriesInstanceUid()).forEach(e -> e.setSeriesId(seriesInfo.getSeriesId()));
        }
    }

    /**
     * 保存实例信息
     */
    private void saveInstanceInfo(List<DicomInstance> dicomInstanceList) {
        List<InstanceInfoPO> instanceInfoList = new ArrayList<>();
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        List<DicomInstance> sortedDicomInstanceList = dicomInstanceList.stream()
                .sorted((o1, o2) -> Double.compare(o2.getSlicePosition(), o1.getSlicePosition())).collect(toList());
        for (int i = 0; i < sortedDicomInstanceList.size(); i++) {
            DicomInstance dicomInstance = sortedDicomInstanceList.get(i);
            InstanceInfoPO instanceInfoPO = BeanUtil.copyProperties(dicomInstance, InstanceInfoPO.class);
            instanceInfoPO.setViewIndex(i + 1);
            instanceInfoList.add(instanceInfoPO);
            ContextFSObject<String> contextFSObject;
            try {
                contextFSObject = new ContextFSObject<>(dicomInstance.getFile());
            } catch (IOException e) {
                log.error(e);
                throw new AppException("保存dicom文件异常");
            }
            contextFSObject.setContext(dicomInstance.getSopInstanceUid());
            contextFSObjects.add(contextFSObject);
        }
        List<ContextUploadResult<String>> uploadResults = fileStoreService.upload(contextFSObjects);
        Map<InstanceInfoPO, ContextUploadResult<String>> uploadMap = CollUtil.zip(instanceInfoList, uploadResults);
        for (InstanceInfoPO instanceInfo : instanceInfoList) {
            instanceInfo.setAccessPath(uploadMap.get(instanceInfo).getAccessPath());
        }
        instanceInfoMapper.insertBatch(instanceInfoList);
    }

    /**
     * 创建计算序列
     */
    private List<ComputeSeriesPO> createComputeSeries(List<DicomInstance> dicomInstanceList) {
        // 判断计算类型，该判断应用独立的模块根据DICOM信息自动分析出当前影像能够使用哪些诊断
        // 目前暂时以只有1个dicom为脊柱侧弯，否则为肺部诊断
        List<ComputeSeriesPO> computeSeriesList = new ArrayList<>();
        if (dicomInstanceList.size() == 1) {
            DicomInstance dicomInstance = dicomInstanceList.get(0);
            ComputeSeriesPO computeSeries = new ComputeSeriesPO();
            computeSeries.setCaseId(dicomInstance.getCaseId());
            computeSeries.setStudyId(dicomInstance.getStudyId());
            computeSeries.setSeriesId(dicomInstance.getSeriesId());
            computeSeries.setComputeType(ComputeType.SPINE);
            computeSeries.setComputeStatus(ComputeStatus.WAIT_COMPUTE);
            computeSeriesMapper.insert(computeSeries);
            computeSeriesList.add(computeSeries);
        } else {
            Map<String, List<DicomInstance>> group = dicomInstanceList
                    .stream().collect(groupingBy(DicomInstance::getSeriesInstanceUid));
            for (List<DicomInstance> dicomInstances : group.values()) {
                DicomInstance dicomInstance = dicomInstances.get(0);
                ComputeSeriesPO computeSeries = new ComputeSeriesPO();
                computeSeries.setCaseId(dicomInstance.getCaseId());
                computeSeries.setStudyId(dicomInstance.getStudyId());
                computeSeries.setSeriesId(dicomInstance.getSeriesId());
                computeSeries.setComputeType(ComputeType.LUNG);
                computeSeries.setComputeStatus(ComputeStatus.WAIT_COMPUTE);
                computeSeriesList.add(computeSeries);
            }
            computeSeriesMapper.insertBatch(computeSeriesList);
        }
        return computeSeriesList;
    }

    /**
     * 创建子任务
     */
    private void createSubTask(List<ComputeSeriesPO> computeSeriesList) {
        List<KeyaApplyTaskPO> keyaApplyTaskList = new ArrayList<>();
        List<MprTaskPO> mprTaskList = new ArrayList<>();
        for (ComputeSeriesPO computeSeries : computeSeriesList) {
            if (computeSeries.getComputeType() == 1) {
                KeyaApplyTaskPO applyTask = new KeyaApplyTaskPO();
                applyTask.setComputeSeriesId(computeSeries.getComputeSeriesId());
                applyTask.setTaskStatus(0);
                keyaApplyTaskList.add(applyTask);

                MprTaskPO sliceMprTask = new MprTaskPO();
                sliceMprTask.setComputeSeriesId(computeSeries.getComputeSeriesId());
                sliceMprTask.setTaskStatus(0);
                sliceMprTask.setMprType(MprType.SLICE);
                mprTaskList.add(sliceMprTask);

                MprTaskPO lungMprTask = new MprTaskPO();
                lungMprTask.setComputeSeriesId(computeSeries.getComputeSeriesId());
                lungMprTask.setTaskStatus(0);
                lungMprTask.setMprType(MprType.LUNG);
                mprTaskList.add(lungMprTask);

                String mprApplyId = IdUtil.fastSimpleUUID();
                mprTaskList.forEach(it -> it.setApplyId(mprApplyId));
            }
        }
        keyaApplyTaskMapper.insertBatch(keyaApplyTaskList);
        mprTaskMapper.insertBatch(mprTaskList);
    }

    /**
     * 创建一个临时文件夹，用于存储解压后的文件
     */
    private File createCaseFileTempDir(InputStream inputStream) {
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("case_file").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }
        ZipUtil.unzip(inputStream, tempDir, Charset.defaultCharset());
        return tempDir;
    }

    @Override
    public Page<CaseStudyVO> pageCaseStudies(CaseStudyQuery caseStudyQuery, PageRequest pageRequest) {
        PageKit.startPage(pageRequest);
        List<CaseStudyVO> caseStudies = caseMapper.queryCaseStudies(caseStudyQuery, SecurityKit.currentUserId());
        if (CollUtil.isEmpty(caseStudies)) {
            return PageKit.finishPage(caseStudies);
        }
        // 先根据条件查询出检查，再由检查查询序列
        List<String> studyIds = caseStudies.stream().map(CaseStudyVO::getStudyId).collect(toList());
        List<CaseSeriesVO> caseSeriesList = caseMapper.queryCaseSeries(
                studyIds, caseStudyQuery.getComputeType(), caseStudyQuery.getComputeStatus());
        Map<String, List<CaseSeriesVO>> group = caseSeriesList
                .stream().collect(groupingBy(CaseSeriesVO::getStudyId));
        for (CaseStudyVO caseStudy : caseStudies) {
            List<CaseSeriesVO> caseSeriesGroupList = group.get(caseStudy.getStudyId());
            caseSeriesGroupList.forEach(caseSeries -> caseSeries.setMyFavorite(caseStudy.getMyFavorite()));
            caseStudy.setCaseSeriesList(caseSeriesGroupList);
            caseStudy.setSeriesCount(caseSeriesGroupList.size());
            Set<Integer> computeTypes = caseSeriesGroupList.stream()
                    .map(CaseSeriesVO::getComputeType)
                    .collect(toSet());
            caseStudy.setComputeType(Joiner.on(",").join(computeTypes));
        }
        return PageKit.finishPage(caseStudies);
    }

    @Override
    public void addFavorite(String studyId) {
        checkStudyInfoExists(studyId);
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
        checkStudyInfoExists(studyId);
        String currentUserId = SecurityKit.currentUserId();
        boolean favoriteExists = favoriteMapper.exists(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(favoriteExists, "该检查没有被收藏，不需要取消收藏");
        int deleted = favoriteMapper.delete(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(deleted > 0, "取消收藏失败");
    }

    @Override
    public void deleteCaseStudy(String studyId) {
        checkStudyInfoExists(studyId);
        int deleted = studyInfoMapper.deleteById(studyId);
        AppAssert.isTrue(deleted == 1, "删除检查失败");
    }

    @Override
    public void deleteCaseSeries(String computeSeriesId) {
        checkComputeSeriesExists(computeSeriesId);
        int deleted = computeSeriesMapper.deleteById(computeSeriesId);
        AppAssert.isTrue(deleted == 1, "删除序列失败");
    }

    @Override
    public void recomputeStudy(String studyId) {
        checkStudyInfoExists(studyId);
        List<ComputeSeriesPO> computeSeriesList = computeSeriesMapper.selectList(
                Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getStudyId, studyId));
        for (ComputeSeriesPO computeSeries : computeSeriesList) {
            if (computeSeries.getComputeStatus() == 2) {
                throw new AppException("存在计算中的序列，无法发起重新分析");
            }
        }
        List<String> computeSeriesIds = computeSeriesList.stream().map(ComputeSeriesPO::getComputeSeriesId).collect(toList());
        resetSubTasks(computeSeriesIds);
    }

    @Override
    public void recomputeSeries(String computeSeriesId) {
        checkComputeSeriesExists(computeSeriesId);
        resetSubTasks(ListUtil.of(computeSeriesId));
    }

    private void checkStudyInfoExists(String studyId) {
        boolean studyExists = studyInfoMapper.exists(
                Wrappers.<StudyInfoPO>lambdaQuery().eq(StudyInfoPO::getStudyId, studyId));
        AppAssert.isTrue(studyExists, "该检查不存在！");
    }

    private void checkComputeSeriesExists(String computeSeriesId) {
        boolean taskExists = computeSeriesMapper.exists(
                Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId));
        AppAssert.isTrue(taskExists, "该序列不存在！");
    }

    private void resetSubTasks(List<String> computeSeriesIds) {
        keyaApplyTaskMapper.update(new KeyaApplyTaskPO(), Wrappers.<KeyaApplyTaskPO>lambdaUpdate()
                .in(KeyaApplyTaskPO::getComputeSeriesId, computeSeriesIds)
                .set(KeyaApplyTaskPO::getTaskStatus, 0));
        keyaQueryTaskMapper.delete(
                Wrappers.<KeyaQueryTaskPO>lambdaUpdate().in(KeyaQueryTaskPO::getComputeSeriesId, computeSeriesIds));
        // MPR成功的，不用再重新发起MPR
        mprTaskMapper.update(new MprTaskPO(), Wrappers.<MprTaskPO>lambdaUpdate()
                .in(MprTaskPO::getComputeSeriesId, computeSeriesIds)
                .and(w -> w.isNull(MprTaskPO::getPushResult).or().ne(MprTaskPO::getMprResult, 1))
                .set(MprTaskPO::getApplyId, IdUtil.fastSimpleUUID())
                .set(MprTaskPO::getTaskStatus, 0));
    }
}
