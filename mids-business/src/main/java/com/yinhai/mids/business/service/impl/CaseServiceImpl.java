package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
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
    private ComputeTaskMapper computeTaskMapper;

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
            createSubTask(createComputeTask(dicomInstanceList));
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
     * 保存检查信息
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
            group.get(studyInfo.getStudyInstanceUid()).forEach(e -> e.setStudyInfoId(studyInfo.getStudyInfoId()));
        }
    }

    /**
     * 保存序列信息
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
            group.get(seriesInfo.getSeriesInstanceUid()).forEach(e -> e.setSeriesInfoId(seriesInfo.getSeriesInfoId()));
        }
    }

    /**
     * 保存实例信息
     */
    private void saveInstanceInfo(List<DicomInstance> dicomInstanceList) {
        List<InstanceInfoPO> instanceInfoList = new ArrayList<>();
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        for (DicomInstance dicomInstance : dicomInstanceList) {
            InstanceInfoPO instanceInfoPO = BeanUtil.copyProperties(dicomInstance, InstanceInfoPO.class);
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
     * 创建计算任务
     */
    private List<ComputeTaskPO> createComputeTask(List<DicomInstance> dicomInstanceList) {
        // 判断计算类型，该判断应用独立的模块根据DICOM信息自动分析出当前影像能够使用哪些诊断
        // 目前暂时以只有1个dicom为脊柱侧弯，否则为肺部诊断
        List<ComputeTaskPO> computeTaskList = new ArrayList<>();
        if (dicomInstanceList.size() == 1) {
            DicomInstance dicomInstance = dicomInstanceList.get(0);
            ComputeTaskPO computeTask = new ComputeTaskPO();
            computeTask.setCaseId(dicomInstance.getCaseId());
            computeTask.setStudyInfoId(dicomInstance.getStudyInfoId());
            computeTask.setSeriesInfoId(dicomInstance.getSeriesInfoId());
            computeTask.setComputeType(ComputeType.SPINE);
            computeTask.setComputeStatus(Integer.valueOf(ComputeStatus.WAIT_COMPUTE));
            computeTaskMapper.insert(computeTask);
            computeTaskList.add(computeTask);
        } else {
            Map<String, List<DicomInstance>> group = dicomInstanceList
                    .stream().collect(groupingBy(DicomInstance::getSeriesInstanceUid));
            for (List<DicomInstance> dicomInstances : group.values()) {
                DicomInstance dicomInstance = dicomInstances.get(0);
                ComputeTaskPO computeTask = new ComputeTaskPO();
                computeTask.setCaseId(dicomInstance.getCaseId());
                computeTask.setStudyInfoId(dicomInstance.getStudyInfoId());
                computeTask.setSeriesInfoId(dicomInstance.getSeriesInfoId());
                computeTask.setComputeType(ComputeType.LUNG);
                computeTask.setComputeStatus(Integer.valueOf(ComputeStatus.WAIT_COMPUTE));
                computeTaskList.add(computeTask);
            }
            computeTaskMapper.insertBatch(computeTaskList);
        }
        return computeTaskList;
    }

    /**
     * 创建子任务
     */
    private void createSubTask(List<ComputeTaskPO> computeTaskList) {
        List<KeyaApplyTaskPO> keyaApplyTaskList = new ArrayList<>();
        List<MprTaskPO> mprTaskList = new ArrayList<>();
        for (ComputeTaskPO computeTask : computeTaskList) {
            if (computeTask.getComputeType() == 1) {
                KeyaApplyTaskPO applyTask = new KeyaApplyTaskPO();
                applyTask.setComputeTaskId(computeTask.getComputeTaskId());
                applyTask.setTaskStatus(0);
                keyaApplyTaskList.add(applyTask);

                MprTaskPO mprTask = new MprTaskPO();
                mprTask.setComputeTaskId(computeTask.getComputeTaskId());
                mprTask.setTaskStatus(0);
                mprTaskList.add(mprTask);
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
        List<String> studyInfoIds = caseStudies.stream().map(CaseStudyVO::getStudyInfoId).collect(toList());
        List<CaseSeriesVO> caseSeriesList = caseMapper.queryCaseSeries(
                studyInfoIds, caseStudyQuery.getComputeType(), caseStudyQuery.getComputeStatus());
        Map<String, List<CaseSeriesVO>> group = caseSeriesList
                .stream().collect(groupingBy(CaseSeriesVO::getStudyInfoId));
        for (CaseStudyVO caseStudy : caseStudies) {
            List<CaseSeriesVO> caseSeriesGroupList = group.get(caseStudy.getStudyInfoId());
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
    public void addFavorite(String studyInfoId) {
        checkStudyInfoExists(studyInfoId);
        String currentUserId = SecurityKit.currentUserId();
        boolean favoriteExists = favoriteMapper.exists(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyInfoId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isFalse(favoriteExists, "该检查已经收藏，无需重复收藏");
        FavoritePO favoritePO = new FavoritePO();
        favoritePO.setUserId(currentUserId);
        favoritePO.setStudyId(studyInfoId);
        int inserted = favoriteMapper.insert(favoritePO);
        AppAssert.isTrue(inserted == 1, "收藏失败");
    }

    @Override
    public void removeFavorite(String studyInfoId) {
        checkStudyInfoExists(studyInfoId);
        String currentUserId = SecurityKit.currentUserId();
        boolean favoriteExists = favoriteMapper.exists(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyInfoId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(favoriteExists, "该检查没有被收藏，不需要取消收藏");
        int deleted = favoriteMapper.delete(Wrappers.<FavoritePO>lambdaQuery()
                .eq(FavoritePO::getStudyId, studyInfoId).eq(FavoritePO::getUserId, currentUserId));
        AppAssert.isTrue(deleted > 0, "取消收藏失败");
    }

    @Override
    public void deleteCaseStudy(String studyInfoId) {
        checkStudyInfoExists(studyInfoId);
        int deleted = studyInfoMapper.deleteById(studyInfoId);
        AppAssert.isTrue(deleted == 1, "删除检查失败");
    }

    @Override
    public void deleteCaseSeries(String computeTaskId) {
        checkComputeTaskExists(computeTaskId);
        int deleted = computeTaskMapper.deleteById(computeTaskId);
        AppAssert.isTrue(deleted == 1, "删除序列失败");
    }

    @Override
    public void reComputeStudy(String studyInfoId) {
        checkStudyInfoExists(studyInfoId);
        List<ComputeTaskPO> computeTaskList = computeTaskMapper.selectList(
                Wrappers.<ComputeTaskPO>lambdaQuery().eq(ComputeTaskPO::getStudyInfoId, studyInfoId));
        for (ComputeTaskPO computeTask : computeTaskList) {
            if (computeTask.getComputeStatus() == 2) {
                throw new AppException("存在计算中的序列，无法发起重新分析");
            }
        }
        List<String> computeTaskIds = computeTaskList.stream().map(ComputeTaskPO::getComputeTaskId).collect(toList());
        resetSubTasks(computeTaskIds);
    }

    @Override
    public void reComputeSeries(String computeTaskId) {
        checkComputeTaskExists(computeTaskId);
        resetSubTasks(ListUtil.of(computeTaskId));
    }

    private void checkStudyInfoExists(String studyInfoId) {
        boolean studyExists = studyInfoMapper.exists(
                Wrappers.<StudyInfoPO>lambdaQuery().eq(StudyInfoPO::getStudyInfoId, studyInfoId));
        AppAssert.isTrue(studyExists, "该检查不存在！");
    }

    private void checkComputeTaskExists(String computeTaskId) {
        boolean taskExists = computeTaskMapper.exists(
                Wrappers.<ComputeTaskPO>lambdaQuery().eq(ComputeTaskPO::getComputeTaskId, computeTaskId));
        AppAssert.isTrue(taskExists, "该序列不存在！");
    }

    private void resetSubTasks(List<String> computeTaskIds) {
        keyaApplyTaskMapper.update(new KeyaApplyTaskPO(), Wrappers.<KeyaApplyTaskPO>lambdaUpdate()
                .in(KeyaApplyTaskPO::getComputeTaskId, computeTaskIds)
                .set(KeyaApplyTaskPO::getTaskStatus, 0));
        keyaQueryTaskMapper.delete(
                Wrappers.<KeyaQueryTaskPO>lambdaUpdate().in(KeyaQueryTaskPO::getComputeTaskId, computeTaskIds));
        mprTaskMapper.update(new MprTaskPO(), Wrappers.<MprTaskPO>lambdaUpdate()
                .in(MprTaskPO::getComputeTaskId, computeTaskIds)
                .set(MprTaskPO::getTaskStatus, 0));
    }
}
