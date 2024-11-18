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
import com.yinhai.mids.business.entity.vo.PneumoniaContourVO;
import com.yinhai.mids.business.entity.vo.PneumoniaLesionVO;
import com.yinhai.mids.business.entity.vo.PneumoniaVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.PneumoniaService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/28
 */
@Service
@TaTransactional
public class PneumoniaServiceImpl implements PneumoniaService {

    @Resource
    private PneumoniaLesionMapper pneumoniaLesionMapper;

    @Resource
    private PneumoniaContourMapper pneumoniaContourMapper;

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
    public void resetPneumoniaOperate(String computeSeriesId) {
        // 重置病变内容
        pneumoniaLesionMapper.delete(Wrappers.<PneumoniaLesionPO>lambdaQuery()
                .eq(PneumoniaLesionPO::getDataType, 1)
                .eq(PneumoniaLesionPO::getComputeSeriesId, computeSeriesId));
        // 清空人工诊断结果
        manualDiagnosisMapper.delete(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getType, DiagnosisType.PNEUMONIA)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        // 清空报告
        clearPneumoniaReport(computeSeriesId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PneumoniaVO queryPneumonia(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeries, "该序列不存在！");
        AppAssert.equals(computeSeries.getComputeStatus(), ComputeStatus.COMPUTE_SUCCESS, "当前序列计算状态非成功状态，无法查看肺炎情况");
        SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(computeSeries.getSeriesId());
        AppAssert.notNull(seriesInfo, "该序列不存在！");

        DiagnosisPO diagnosisPO = diagnosisMapper.selectOne(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.PNEUMONIA)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(diagnosisPO, "计算结果丢失");

        PneumoniaVO pneumoniaVO = new PneumoniaVO();
        pneumoniaVO.setComputeSeriesId(computeSeriesId);
        pneumoniaVO.setStudyId(seriesInfo.getStudyId());
        pneumoniaVO.setSeriesId(seriesInfo.getSeriesId());
        pneumoniaVO.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        pneumoniaVO.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        pneumoniaVO.setHasLesion(diagnosisPO.getHasLesion());

        List<PneumoniaLesionPO> manualList = pneumoniaLesionMapper.selectList(Wrappers.<PneumoniaLesionPO>lambdaQuery()
                .eq(PneumoniaLesionPO::getDataType, 1)
                .eq(PneumoniaLesionPO::getComputeSeriesId, computeSeriesId));
        if (CollUtil.isEmpty(manualList)) {
            List<PneumoniaLesionPO> sourceList = pneumoniaLesionMapper.selectList(Wrappers.<PneumoniaLesionPO>lambdaQuery()
                    .eq(PneumoniaLesionPO::getDataType, 0).eq(PneumoniaLesionPO::getComputeSeriesId, computeSeriesId));
            CopyOptions copyOptions = new CopyOptions().setIgnoreProperties(
                    PneumoniaLesionPO::getId, PneumoniaLesionPO::getCreateTime, PneumoniaLesionPO::getUpdateTime);
            manualList = BeanUtil.copyToList(sourceList, PneumoniaLesionPO.class, copyOptions);
            manualList.forEach(i -> i.setDataType(1));
            pneumoniaLesionMapper.insert(manualList);
        }
        List<PneumoniaLesionVO> pneumoniaLesionList = BeanUtil.copyToList(manualList, PneumoniaLesionVO.class);
        pneumoniaVO.setPneumoniaLesionList(pneumoniaLesionList);

        List<PneumoniaContourPO> pneumoniaContourList = pneumoniaContourMapper.selectList(
                Wrappers.<PneumoniaContourPO>lambdaQuery().eq(PneumoniaContourPO::getComputeSeriesId, computeSeriesId));
        pneumoniaVO.setPneumoniaContourList(BeanUtil.copyToList(pneumoniaContourList, PneumoniaContourVO.class));

        return pneumoniaVO;
    }

    @Override
    public void updateCheckStatus(String pneumoniaLesionId, Boolean checked) {
        PneumoniaLesionPO pneumoniaLesionPO = new PneumoniaLesionPO();
        pneumoniaLesionPO.setId(pneumoniaLesionId);
        pneumoniaLesionPO.setChecked(checked);
        int updated = pneumoniaLesionMapper.updateById(pneumoniaLesionPO);
        AppAssert.isTrue(updated == 1, "更新选中状态失败");
    }

    @Override
    public void savePneumoniaManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam) {
        String computeSeriesId = manualDiagnosisParam.getComputeSeriesId();
        ManualDiagnosisPO one = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .select(ManualDiagnosisPO::getId)
                .eq(ManualDiagnosisPO::getType, DiagnosisType.PNEUMONIA)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        if (one != null) {
            manualDiagnosisMapper.update(new ManualDiagnosisPO(), Wrappers.<ManualDiagnosisPO>lambdaUpdate()
                    .eq(ManualDiagnosisPO::getId, one.getId())
                    .eq(ManualDiagnosisPO::getType, DiagnosisType.PNEUMONIA)
                    .set(ManualDiagnosisPO::getDiagnosis, manualDiagnosisParam.getDiagnosis())
                    .set(ManualDiagnosisPO::getFinding, manualDiagnosisParam.getFinding())
                    .set(ManualDiagnosisPO::getDiagnoseTime, DbClock.now()));
        } else {
            ManualDiagnosisPO manualDiagnosisPO = new ManualDiagnosisPO();
            manualDiagnosisPO.setComputeSeriesId(computeSeriesId);
            manualDiagnosisPO.setType(DiagnosisType.PNEUMONIA);
            manualDiagnosisPO.setDiagnosis(manualDiagnosisParam.getDiagnosis());
            manualDiagnosisPO.setFinding(manualDiagnosisParam.getFinding());
            manualDiagnosisPO.setDiagnoseTime(DbClock.now());
            manualDiagnosisMapper.insert(manualDiagnosisPO);
        }
         // 清空报告
        clearPneumoniaReport(computeSeriesId);
    }

    @Override
    public TextReportVO queryTextReport(String computeSeriesId, Boolean reset) {
        boolean resetReport = BooleanUtil.isTrue(reset);
        if (resetReport) {
            textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery()
                    .eq(TextReportPO::getReportType, DiagnosisType.PNEUMONIA)
                    .eq(TextReportPO::getComputeSeriesId, computeSeriesId));
            return generateAndSaveTextReport(computeSeriesId);
        }
        TextReportPO textReportPO = textReportMapper.selectOne(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getReportType, DiagnosisType.PNEUMONIA)
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
        reportPO.setReportType(DiagnosisType.PNEUMONIA);
        textReportMapper.insert(reportPO);
        textReportVO.setId(reportPO.getId());

        return textReportVO;
    }

    private ReportCommon queryReportCommon(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectOne(Wrappers.<ComputeSeriesPO>lambdaQuery()
                .select(ComputeSeriesPO::getStudyId)
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(computeSeries, "该序列不存在！");
        StudyInfoPO studyInfo = studyInfoMapper.selectById(computeSeries.getStudyId());
        AppAssert.notNull(studyInfo, "该序列对应检查不存在！");

        ManualDiagnosisPO manualDiagnosisPO = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getType, DiagnosisType.PNEUMONIA)
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
     * 清空现有肺炎影像报告
     */
    private void clearPneumoniaReport(String computeSeriesId) {
        textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getComputeSeriesId, computeSeriesId)
                .eq(TextReportPO::getReportType, DiagnosisType.PNEUMONIA));
    }
}
