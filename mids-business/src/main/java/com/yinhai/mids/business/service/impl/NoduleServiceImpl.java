package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.dto.NoduleOperate;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.NoduleLesionVO;
import com.yinhai.mids.business.entity.vo.NoduleOperateVO;
import com.yinhai.mids.business.entity.vo.NoduleVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.NoduleService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/8/22
 */
@Service
@TaTransactional
public class NoduleServiceImpl implements NoduleService {

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private DiagnosisMapper diagnosisMapper;

    @Resource
    private NoduleLesionMapper noduleLesionMapper;

    @Resource
    private NoduleOperateMapper noduleOperateMapper;

    @Resource
    private ManualDiagnosisMapper manualDiagnosisMapper;

    @Resource
    private TextReportMapper textReportMapper;

    @Override
    public NoduleOperateVO queryNoduleOperate(String computeSeriesId) {
        NoduleOperatePO noduleOperatePO = noduleOperateMapper.selectOne(
                Wrappers.<NoduleOperatePO>lambdaQuery().eq(NoduleOperatePO::getComputeSeriesId, computeSeriesId));
        if (noduleOperatePO == null) {
            NoduleOperateVO noduleOperateVO = new NoduleOperateVO();
            noduleOperateVO.setLesionOrderType("1");
            // TODO 完善其余默认条件
            return noduleOperateVO;
        }
        return BeanUtil.copyProperties(noduleOperatePO, NoduleOperateVO.class);
    }

    @Override
    public void saveNoduleOperate(NoduleOperateVO noduleOperateVO) {
        NoduleOperate operate = convertNoduleOperate(noduleOperateVO);
        NoduleOperatePO noduleOperatePO = new NoduleOperatePO();
        Joiner joiner = Joiner.on(StrPool.COMMA);
        noduleOperatePO.setComputeSeriesId(operate.getComputeSeriesId());
        noduleOperatePO.setLesionOrderType(operate.getLesionOrderType());
        noduleOperatePO.setRiskFilter(joiner.join(operate.getRiskFilterList()));
        noduleOperatePO.setTypeFilter(joiner.join(operate.getTypeFilterList()));
        noduleOperatePO.setMajorAxisSelectFilter(joiner.join(operate.getMajorAxisSelectFilterList()));
        noduleOperatePO.setMajorAxisScopeFilter(joiner.join(operate.getMajorAxisMin(), operate.getMajorAxisMax()));
        noduleOperatePO.setFindingOrderType(operate.getFindingOrderType());
        noduleOperatePO.setDiagnosisType(operate.getDiagnosisType());
        noduleOperatePO.setNoduleSelect(joiner.join(operate.getNoduleSelectList()));
        noduleOperatePO.setOperateTime(MapperKit.executeForDate());
        NoduleOperatePO one = noduleOperateMapper.selectOne(Wrappers.<NoduleOperatePO>lambdaQuery()
                .eq(NoduleOperatePO::getComputeSeriesId, operate.getComputeSeriesId()));
        if (one != null) {
            noduleOperatePO.setId(one.getId());
            noduleOperateMapper.updateById(noduleOperatePO);
        } else {
            noduleOperateMapper.insert(noduleOperatePO);
        }
        clearNoduleReport(operate.getComputeSeriesId());
    }

    @Override
    public NoduleVO queryNodule(String computeSeriesId, Boolean reset) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeriesPO, "该序列不存在！");
        AppAssert.equals(computeSeriesPO.getComputeStatus(), ComputeStatus.COMPUTE_SUCCESS, "当前序列计算状态非成功状态，无法查看结节情况");
        SeriesPO seriesPO = seriesMapper.selectById(computeSeriesPO.getSeriesId());
        AppAssert.notNull(seriesPO, "该序列不存在！");

        DiagnosisPO diagnosisPO = diagnosisMapper.selectOne(
                Wrappers.<DiagnosisPO>lambdaQuery().eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(diagnosisPO, "计算结果丢失");

        NoduleVO noduleVO = new NoduleVO();
        noduleVO.setComputeSeriesId(computeSeriesId);
        noduleVO.setStudyId(seriesPO.getStudyId());
        noduleVO.setSeriesId(seriesPO.getId());
        noduleVO.setStudyInstanceUid(seriesPO.getStudyInstanceUid());
        noduleVO.setSeriesInstanceUid(seriesPO.getSeriesInstanceUid());
        noduleVO.setHasLesion(diagnosisPO.getHasLesion());
        noduleVO.setImageCount(seriesPO.getImageCount());
        List<NoduleLesionPO> noduleLesionPOList = noduleLesionMapper.selectList(
                Wrappers.<NoduleLesionPO>lambdaQuery().eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));
        noduleVO.setNoduleLesionList(BeanUtil.copyToList(noduleLesionPOList, NoduleLesionVO.class));
        return noduleVO;
    }

    @Override
    public void updateNoduleLesion(NoduleLesionVO noduleLesionVO) {
        NoduleLesionPO noduleLesionPO = BeanUtil.copyProperties(noduleLesionVO, NoduleLesionPO.class);
        noduleLesionMapper.updateById(noduleLesionPO);
        clearNoduleReport(noduleLesionVO.getComputeSeriesId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveNoduleManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam) {
        String computeSeriesId = manualDiagnosisParam.getComputeSeriesId();
        ManualDiagnosisPO manualDiagnosisPO = new ManualDiagnosisPO();
        manualDiagnosisPO.setComputeSeriesId(computeSeriesId);
        manualDiagnosisPO.setType("nodule");
        manualDiagnosisPO.setDiagnosis(manualDiagnosisParam.getDiagnosis());
        manualDiagnosisPO.setFinding(manualDiagnosisParam.getFinding());
        manualDiagnosisPO.setDiagnoseTime(MapperKit.executeForDate());
        ManualDiagnosisPO one = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .select(ManualDiagnosisPO::getId)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        if (one != null) {
            manualDiagnosisPO.setId(one.getId());
            manualDiagnosisMapper.updateById(manualDiagnosisPO);
        } else {
            manualDiagnosisMapper.insert(manualDiagnosisPO);
        }
    }

    @Override
    public TextReportVO queryTextReport(String computeSeriesId, Boolean reset) {
        boolean resetReport = BooleanUtil.isTrue(reset);
        if (resetReport) {
            return generateAndSaveTextReport(computeSeriesId);
        }
        TextReportPO textReportPO = textReportMapper.selectOne(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getReportType, "nodule")
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

    /**
     * 对结节操作进行校验和转换
     *
     * @param noduleOperateVO noduleOperateVO
     * @return {@link NoduleOperate }
     * @author zhuhs 2024/08/21
     */
    private NoduleOperate convertNoduleOperate(NoduleOperateVO noduleOperateVO) {
        NoduleOperate noduleOperate = new NoduleOperate();
        noduleOperate.setComputeSeriesId(noduleOperateVO.getComputeSeriesId());

        String lesionOrderType = noduleOperateVO.getLesionOrderType();
        if (StrUtil.isNotBlank(lesionOrderType)) {
            AppAssert.isTrue(StrUtil.equalsAny(lesionOrderType, "1", "2", "3", "4", "5", "6"), "病变排序类型不正确");
            noduleOperate.setLesionOrderType(lesionOrderType);
        }

        List<String> riskFilterList = StrUtil.split(noduleOperateVO.getRiskFilter(), StrPool.COMMA, true, true);
        if (CollUtil.isNotEmpty(riskFilterList)) {
            riskFilterList = CollUtil.distinct(riskFilterList);
            AppAssert.isTrue(CollUtil.allMatch(riskFilterList, s -> StrUtil.equalsAny(s, "1", "2", "3")), "病变良恶性不正确");
            noduleOperate.setRiskFilterList(riskFilterList);
        }

        List<String> typeFilterList = StrUtil.split(noduleOperateVO.getTypeFilter(), StrPool.COMMA, true, true);
        if (CollUtil.isNotEmpty(typeFilterList)) {
            typeFilterList = CollUtil.distinct(typeFilterList);
            AppAssert.isTrue(CollUtil.allMatch(typeFilterList,
                    s -> StrUtil.equalsAny(s, "GCN", "Solid", "Calcified", "Mixed", "Mass")), "病变类型不正确");
            noduleOperate.setTypeFilterList(typeFilterList);
        }

        if (StrUtil.isBlank(noduleOperateVO.getMajorAxisScopeFilter())) {
            List<String> majorAxisSelectFilterList = StrUtil.split(noduleOperateVO.getMajorAxisSelectFilter(), StrPool.COMMA, true, true);
            if (CollUtil.isNotEmpty(majorAxisSelectFilterList)) {
                majorAxisSelectFilterList = CollUtil.distinct(majorAxisSelectFilterList);
                AppAssert.isTrue(CollUtil.allMatch(majorAxisSelectFilterList,
                        s -> StrUtil.equalsAny(s, "1", "2", "3", "4")), "病变长径不正确");
                noduleOperate.setMajorAxisSelectFilterList(majorAxisSelectFilterList);
            }
        }

        List<String> majorAxisScopeFilterList = StrUtil.split(noduleOperateVO.getMajorAxisScopeFilter(), StrPool.COMMA, true, true);
        if (CollUtil.isNotEmpty(majorAxisScopeFilterList)) {
            AppAssert.isTrue(majorAxisScopeFilterList.size() == 2, "病变长径范围不正确");
            String min = majorAxisScopeFilterList.get(0);
            String max = majorAxisScopeFilterList.get(1);
            AppAssert.isTrue(NumberUtil.isDouble(min), "病变长径范围最小值不正确");
            AppAssert.isTrue(NumberUtil.isDouble(max), "病变长径范围最大值不正确");
            AppAssert.isTrue(Double.parseDouble(min) <= Double.parseDouble(max), "病变长径范围不正确");
            noduleOperate.setMajorAxisMin(Double.parseDouble(min));
            noduleOperate.setMajorAxisMax(Double.parseDouble(max));
        }

        String findingOrderType = noduleOperateVO.getFindingOrderType();
        if (StrUtil.isNotBlank(findingOrderType)) {
            AppAssert.isTrue(StrUtil.equalsAny(findingOrderType, "1", "2", "3", "4", "5"), "影像所见排序类型不正确");
            noduleOperate.setFindingOrderType(findingOrderType);
        }

        String diagnosisType = noduleOperateVO.getDiagnosisType();
        if (StrUtil.isNotBlank(diagnosisType)) {
            AppAssert.isTrue(StrUtil.equalsAny(diagnosisType, "1", "2"), "影像诊断类型不正确");
            noduleOperate.setDiagnosisType(diagnosisType);
        }
        List<String> noduleSelectList = StrUtil.split(noduleOperateVO.getNoduleSelect(), StrPool.COMMA, true, true);
        noduleOperate.setNoduleSelectList(noduleSelectList);
        return noduleOperate;
    }

    @SuppressWarnings("unchecked")
    private TextReportVO generateAndSaveTextReport(String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectOne(Wrappers.<ComputeSeriesPO>lambdaQuery()
                .select(ComputeSeriesPO::getStudyId)
                .eq(ComputeSeriesPO::getId, computeSeriesId));
        AppAssert.notNull(computeSeriesPO, "该序列不存在！");
        StudyPO studyPO = studyMapper.selectById(computeSeriesPO.getStudyId());
        AppAssert.notNull(studyPO, "该序列对应检查不存在！");

        ManualDiagnosisPO manualDiagnosisPO = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(manualDiagnosisPO, "该序列对应诊断不存在！");

        TextReportVO textReportVO = new TextReportVO();
        textReportVO.setComputeSeriesId(computeSeriesId);
        textReportVO.setPatientId(studyPO.getPatientId());
        textReportVO.setAccessionNumber(studyPO.getAccessionNumber());
        textReportVO.setStudyDate(studyPO.getStudyDateAndTime());
        textReportVO.setPatientName(studyPO.getPatientName());
        String patientSex = studyPO.getPatientSex();
        if (StrUtil.equals(patientSex, "M")) {
            textReportVO.setPatientSex("男");
        } else if (StrUtil.equals(patientSex, "F")) {
            textReportVO.setPatientSex("女");
        } else {
            textReportVO.setPatientSex("");
        }
        textReportVO.setFinding(manualDiagnosisPO.getFinding());
        textReportVO.setDiagnosis(manualDiagnosisPO.getDiagnosis());
        textReportVO.setReportDate(MapperKit.executeForDate());
        textReportVO.setReportDoctor("");
        textReportVO.setAuditDoctor("");

        TextReportPO reportPO = BeanUtil.copyProperties(textReportVO, TextReportPO.class);
        reportPO.setReportType("nodule");
        textReportMapper.insert(reportPO);
        textReportVO.setId(reportPO.getId());

        return textReportVO;
    }

    /**
     * 清空现有结节影像报告
     *
     * @param computeSeriesId 计算序列ID
     * @author zhuhs 2024/08/22
     */
    private void clearNoduleReport(String computeSeriesId) {
        textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery().eq(TextReportPO::getComputeSeriesId, computeSeriesId));
    }
}
