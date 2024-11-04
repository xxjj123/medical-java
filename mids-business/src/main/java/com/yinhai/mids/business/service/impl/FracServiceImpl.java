package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.DiagnosisType;
import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.model.ReportCommon;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.FracLesionVO;
import com.yinhai.mids.business.entity.vo.FracVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.FracService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/29
 */
@Service
@TaTransactional
public class FracServiceImpl implements FracService {

    @Resource
    private FracLesionMapper fracLesionMapper;

    @Resource
    private ManualDiagnosisMapper manualDiagnosisMapper;

    @Resource
    private TextReportMapper textReportMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private StudyInfoMapper studyInfoMapper;

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

    @Resource
    private DiagnosisMapper diagnosisMapper;

    @Override
    public void resetFracOperate(String computeSeriesId) {
        // 重置病变内容
        fracLesionMapper.delete(Wrappers.<FracLesionPO>lambdaQuery()
                .eq(FracLesionPO::getDataType, 1)
                .eq(FracLesionPO::getComputeSeriesId, computeSeriesId));
        // 清空人工诊断结果
        manualDiagnosisMapper.delete(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getType, DiagnosisType.FRAC)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        // 清空报告
        clearFracReport(computeSeriesId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FracVO queryFrac(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeries, "该序列不存在！");
        AppAssert.equals(computeSeries.getComputeStatus(), ComputeStatus.COMPUTE_SUCCESS, "当前序列计算状态非成功状态，无法查看骨折情况");
        SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(computeSeries.getSeriesId());
        AppAssert.notNull(seriesInfo, "该序列不存在！");

        DiagnosisPO diagnosisPO = diagnosisMapper.selectOne(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.FRAC)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(diagnosisPO, "计算结果丢失");

        FracVO fracVO = new FracVO();
        fracVO.setComputeSeriesId(computeSeriesId);
        fracVO.setStudyId(seriesInfo.getStudyId());
        fracVO.setSeriesId(seriesInfo.getSeriesId());
        fracVO.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        fracVO.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        fracVO.setHasLesion(diagnosisPO.getHasLesion());

        List<FracLesionPO> manualList = fracLesionMapper.selectList(Wrappers.<FracLesionPO>lambdaQuery()
                .eq(FracLesionPO::getDataType, 1)
                .eq(FracLesionPO::getComputeSeriesId, computeSeriesId));
        if (CollUtil.isEmpty(manualList)) {
            List<FracLesionPO> sourceList = fracLesionMapper.selectList(Wrappers.<FracLesionPO>lambdaQuery()
                    .eq(FracLesionPO::getDataType, 1).eq(FracLesionPO::getComputeSeriesId, computeSeriesId));
            CopyOptions copyOptions = new CopyOptions().setIgnoreProperties(
                    FracLesionPO::getId, FracLesionPO::getCreateTime, FracLesionPO::getUpdateTime);
            manualList = BeanUtil.copyToList(sourceList, FracLesionPO.class, copyOptions);
            manualList.forEach(i -> i.setDataType(1));
            fracLesionMapper.insert(manualList);
        }
        List<FracLesionVO> fracLesionList = BeanUtil.copyToList(manualList, FracLesionVO.class);
        fracVO.setFracLesionList(fracLesionList);

        return fracVO;
    }

    @Override
    public void updateFracLesion(FracLesionVO fracLesionVO) {
        FracLesionPO fracLesionPO = BeanUtil.copyProperties(fracLesionVO, FracLesionPO.class);
        fracLesionMapper.updateById(fracLesionPO);
        clearFracReport(fracLesionVO.getComputeSeriesId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveFracManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam) {
        String computeSeriesId = manualDiagnosisParam.getComputeSeriesId();
        ManualDiagnosisPO one = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .select(ManualDiagnosisPO::getId)
                .eq(ManualDiagnosisPO::getType, DiagnosisType.FRAC)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        if (one != null) {
            manualDiagnosisMapper.update(new ManualDiagnosisPO(), Wrappers.<ManualDiagnosisPO>lambdaUpdate()
                    .eq(ManualDiagnosisPO::getId, one.getId())
                    .eq(ManualDiagnosisPO::getType, DiagnosisType.FRAC)
                    .set(ManualDiagnosisPO::getDiagnosis, manualDiagnosisParam.getDiagnosis())
                    .set(ManualDiagnosisPO::getFinding, manualDiagnosisParam.getFinding())
                    .set(ManualDiagnosisPO::getDiagnoseTime, DbClock.now()));
        } else {
            ManualDiagnosisPO manualDiagnosisPO = new ManualDiagnosisPO();
            manualDiagnosisPO.setComputeSeriesId(computeSeriesId);
            manualDiagnosisPO.setType(DiagnosisType.FRAC);
            manualDiagnosisPO.setDiagnosis(manualDiagnosisParam.getDiagnosis());
            manualDiagnosisPO.setFinding(manualDiagnosisParam.getFinding());
            manualDiagnosisPO.setDiagnoseTime(DbClock.now());
            manualDiagnosisMapper.insert(manualDiagnosisPO);
        }
    }

    @Override
    public TextReportVO queryTextReport(String computeSeriesId, Boolean reset) {
        boolean resetReport = BooleanUtil.isTrue(reset);
        if (resetReport) {
            textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery()
                    .eq(TextReportPO::getReportType, DiagnosisType.FRAC)
                    .eq(TextReportPO::getComputeSeriesId, computeSeriesId));
            return generateAndSaveTextReport(computeSeriesId);
        }
        TextReportPO textReportPO = textReportMapper.selectOne(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getReportType, DiagnosisType.FRAC)
                .eq(TextReportPO::getComputeSeriesId, computeSeriesId));
        if (textReportPO != null) {
            return BeanUtil.copyProperties(textReportPO, TextReportVO.class);
        }
        return generateAndSaveTextReport(computeSeriesId);
    }

    @Override
    public void updateTextReport(TextReportVO textReportVO) {
        textReportMapper.updateById(BeanUtil.copyProperties(textReportVO, TextReportPO.class));
    }

    private TextReportVO generateAndSaveTextReport(String computeSeriesId) {
        ReportCommon reportCommon = queryReportCommon(computeSeriesId);
        TextReportVO textReportVO = BeanUtil.copyProperties(reportCommon, TextReportVO.class);

        TextReportPO reportPO = BeanUtil.copyProperties(textReportVO, TextReportPO.class);
        reportPO.setReportType(DiagnosisType.FRAC);
        textReportMapper.insert(reportPO);
        textReportVO.setId(reportPO.getId());

        return textReportVO;
    }

    @SuppressWarnings("unchecked")
    private ReportCommon queryReportCommon(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectOne(Wrappers.<ComputeSeriesPO>lambdaQuery()
                .select(ComputeSeriesPO::getStudyId)
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(computeSeries, "该序列不存在！");
        StudyInfoPO studyInfo = studyInfoMapper.selectById(computeSeries.getStudyId());
        AppAssert.notNull(studyInfo, "该序列对应检查不存在！");

        ManualDiagnosisPO manualDiagnosisPO = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getType, DiagnosisType.FRAC)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(manualDiagnosisPO, "该序列对应诊断不存在！");

        ReportCommon reportCommon = new ReportCommon();
        reportCommon.setComputeSeriesId(computeSeriesId);
        reportCommon.setPatientId(studyInfo.getPatientId());
        reportCommon.setAccessionNumber(studyInfo.getAccessionNumber());
        reportCommon.setStudyDate(studyInfo.getStudyDateAndTime());
        reportCommon.setPatientName(studyInfo.getPatientName());
        String patientSex = studyInfo.getPatientSex();
        if (StrUtil.equals(patientSex, "M")) {
            reportCommon.setPatientSex("男");
        } else if (StrUtil.equals(patientSex, "F")) {
            reportCommon.setPatientSex("女");
        } else {
            reportCommon.setPatientSex("");
        }
        reportCommon.setPatientAge(studyInfo.getPatientAge());
        reportCommon.setExaminedName("胸部CT平扫");
        reportCommon.setFinding(manualDiagnosisPO.getFinding());
        reportCommon.setDiagnosis(manualDiagnosisPO.getDiagnosis());
        reportCommon.setReportDate(DbClock.now());
        reportCommon.setReportDoctor("");
        reportCommon.setAuditDoctor("");
        return reportCommon;
    }

    /**
     * 清空现有骨折影像报告
     */
    private void clearFracReport(String computeSeriesId) {
        textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getComputeSeriesId, computeSeriesId)
                .eq(TextReportPO::getReportType, DiagnosisType.FRAC));
    }
}
