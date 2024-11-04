package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.ImmutableMap;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.DiagnosisType;
import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.model.ReportCommon;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.*;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.NoduleService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/8/22
 */
@Service
@TaTransactional
public class NoduleServiceImpl implements NoduleService {

    @Resource
    private StudyInfoMapper studyInfoMapper;

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

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

    @Resource
    private GraphicReportMapper graphicReportMapper;

    /**
     * 肺叶
     */
    private static final Map<Integer, String> LOBE_MAP = ImmutableMap.<Integer, String>builder()
            .put(1, "lobe_right_top")
            .put(2, "lobe_right_top")
            .put(3, "lobe_right_top")
            .put(4, "lobe_right_middle")
            .put(5, "lobe_right_middle")
            .put(6, "lobe_right_bottom")
            .put(7, "lobe_right_bottom")
            .put(8, "lobe_right_bottom")
            .put(9, "lobe_right_bottom")
            .put(10, "lobe_right_bottom")
            .put(11, "lobe_left_top")
            .put(12, "lobe_left_top")
            .put(13, "lobe_left_top")
            .put(14, "lobe_left_top")
            .put(15, "lobe_left_bottom")
            .put(16, "lobe_left_bottom")
            .put(17, "lobe_left_bottom")
            .put(18, "lobe_left_bottom")
            .build();

    private static final Map<Integer, Integer> LOBE_SEGMENT_SORT_MAP = ImmutableMap.<Integer, Integer>builder()
            .put(1, 11)
            .put(2, 10)
            .put(3, 9)
            .put(4, 13)
            .put(5, 12)
            .put(6, 15)
            .put(7, 16)
            .put(8, 18)
            .put(9, 17)
            .put(10, 14)
            .put(11, 3)
            .put(12, 4)
            .put(13, 2)
            .put(14, 1)
            .put(15, 6)
            .put(16, 8)
            .put(17, 7)
            .put(18, 5)
            .build();

    private static final Map<String, Integer> TYPE_SORT_MAP = ImmutableMap.<String, Integer>builder()
            .put("GCN", 3)
            .put("Mass", 1)
            .put("Mixed", 2)
            .put("Solid", 4)
            .put("Calcified", 5)
            .build();

    @Override
    public NoduleOperateVO queryNoduleOperate(String computeSeriesId) {
        NoduleOperatePO noduleOperatePO = noduleOperateMapper.selectOne(
                Wrappers.<NoduleOperatePO>lambdaQuery().eq(NoduleOperatePO::getComputeSeriesId, computeSeriesId));
        if (noduleOperatePO != null) {
            return BeanUtil.copyProperties(noduleOperatePO, NoduleOperateVO.class);
        }
        NoduleOperateVO noduleOperateVO = new NoduleOperateVO();
        noduleOperateVO.setComputeSeriesId(computeSeriesId);
        noduleOperateVO.setLesionOrderType("1");
        noduleOperateVO.setMajorAxisSelectFilter("2,3,4");
        noduleOperateVO.setFindingOrderType("1");
        noduleOperateVO.setDiagnosisType("1");
        return noduleOperateVO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveNoduleOperate(NoduleOperateVO noduleOperateVO) {
        validateNoduleOperate(noduleOperateVO);
        NoduleOperatePO noduleOperatePO = new NoduleOperatePO();
        BeanUtil.copyProperties(noduleOperateVO, noduleOperatePO);
        if (noduleOperatePO.getMajorAxisScopeFilter() != null) {
            noduleOperatePO.setMajorAxisSelectFilter(null);
        }
        noduleOperatePO.setOperateTime(DbClock.now());
        NoduleOperatePO one = noduleOperateMapper.selectOne(Wrappers.<NoduleOperatePO>lambdaQuery()
                .select(NoduleOperatePO::getId)
                .eq(NoduleOperatePO::getComputeSeriesId, noduleOperateVO.getComputeSeriesId()));
        if (one != null) {
            noduleOperatePO.setId(one.getId());
            noduleOperateMapper.updateById(noduleOperatePO);
        } else {
            noduleOperateMapper.insert(noduleOperatePO);
        }
        clearNoduleReport(noduleOperateVO.getComputeSeriesId());
    }

    @Override
    public void resetNoduleOperate(String computeSeriesId) {
        // 清空操作
        noduleOperateMapper.delete(Wrappers.<NoduleOperatePO>lambdaQuery().eq(NoduleOperatePO::getComputeSeriesId, computeSeriesId));
        // 重置病变内容
        noduleLesionMapper.delete(Wrappers.<NoduleLesionPO>lambdaQuery()
                .eq(NoduleLesionPO::getDataType, 1)
                .eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));
        // 清空人工诊断结果
        manualDiagnosisMapper.delete(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .eq(ManualDiagnosisPO::getType, DiagnosisType.NODULE)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        // 清空报告
        clearNoduleReport(computeSeriesId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NoduleVO queryNodule(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeries, "该序列不存在！");
        AppAssert.equals(computeSeries.getComputeStatus(), ComputeStatus.COMPUTE_SUCCESS, "当前序列计算状态非成功状态，无法查看结节情况");
        SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(computeSeries.getSeriesId());
        AppAssert.notNull(seriesInfo, "该序列不存在！");

        DiagnosisPO diagnosisPO = diagnosisMapper.selectOne(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.NODULE)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(diagnosisPO, "计算结果丢失");

        NoduleVO noduleVO = new NoduleVO();
        noduleVO.setComputeSeriesId(computeSeriesId);
        noduleVO.setStudyId(seriesInfo.getStudyId());
        noduleVO.setSeriesId(seriesInfo.getSeriesId());
        noduleVO.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        noduleVO.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        noduleVO.setHasLesion(diagnosisPO.getHasLesion());
        noduleVO.setImageCount(seriesInfo.getImageCount());
        List<NoduleLesionPO> manualList = noduleLesionMapper.selectList(Wrappers.<NoduleLesionPO>lambdaQuery()
                .eq(NoduleLesionPO::getDataType, 1)
                .eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));
        if (CollUtil.isEmpty(manualList)) {
            List<NoduleLesionPO> sourceList = noduleLesionMapper.selectList(Wrappers.<NoduleLesionPO>lambdaQuery()
                    .eq(NoduleLesionPO::getDataType, 0)
                    .eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));
            CopyOptions copyOptions = new CopyOptions().setIgnoreProperties(
                    NoduleLesionPO::getId, NoduleLesionPO::getCreateTime, NoduleLesionPO::getUpdateTime);
            manualList = BeanUtil.copyToList(sourceList, NoduleLesionPO.class, copyOptions);
            manualList.forEach(i -> i.setDataType(1));
            manualList.forEach(i -> i.setLobe(LOBE_MAP.get(i.getLobeSegment())));
            noduleLesionMapper.insert(manualList);
        }
        List<NoduleLesionVO> noduleLesionVOList = BeanUtil.copyToList(manualList, NoduleLesionVO.class);
        for (NoduleLesionVO noduleLesionVO : noduleLesionVOList) {
            noduleLesionVO.setLobeSegmentSort(LOBE_SEGMENT_SORT_MAP.get(noduleLesionVO.getLobeSegment()));
            noduleLesionVO.setTypeSort(TYPE_SORT_MAP.get(noduleLesionVO.getType()));
        }
        noduleVO.setNoduleLesionList(noduleLesionVOList);
        return noduleVO;
    }

    @Override
    public void updateNoduleLesion(NoduleLesionVO noduleLesionVO) {
        NoduleLesionPO noduleLesionPO = BeanUtil.copyProperties(noduleLesionVO, NoduleLesionPO.class);
        noduleLesionPO.setLobe(LOBE_MAP.get(noduleLesionPO.getLobeSegment()));
        noduleLesionMapper.updateById(noduleLesionPO);
        clearNoduleReport(noduleLesionVO.getComputeSeriesId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveNoduleManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam) {
        String computeSeriesId = manualDiagnosisParam.getComputeSeriesId();
        ManualDiagnosisPO one = manualDiagnosisMapper.selectOne(Wrappers.<ManualDiagnosisPO>lambdaQuery()
                .select(ManualDiagnosisPO::getId)
                .eq(ManualDiagnosisPO::getType, DiagnosisType.NODULE)
                .eq(ManualDiagnosisPO::getComputeSeriesId, computeSeriesId));
        if (one != null) {
            manualDiagnosisMapper.update(new ManualDiagnosisPO(), Wrappers.<ManualDiagnosisPO>lambdaUpdate()
                    .eq(ManualDiagnosisPO::getId, one.getId())
                    .eq(ManualDiagnosisPO::getType, DiagnosisType.NODULE)
                    .set(ManualDiagnosisPO::getDiagnosis, manualDiagnosisParam.getDiagnosis())
                    .set(ManualDiagnosisPO::getFinding, manualDiagnosisParam.getFinding())
                    .set(ManualDiagnosisPO::getDiagnoseTime, DbClock.now()));
        } else {
            ManualDiagnosisPO manualDiagnosisPO = new ManualDiagnosisPO();
            manualDiagnosisPO.setComputeSeriesId(computeSeriesId);
            manualDiagnosisPO.setType(DiagnosisType.NODULE);
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
                    .eq(TextReportPO::getReportType, DiagnosisType.NODULE)
                    .eq(TextReportPO::getComputeSeriesId, computeSeriesId));
            return generateAndSaveTextReport(computeSeriesId);
        }
        TextReportPO textReportPO = textReportMapper.selectOne(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getReportType, DiagnosisType.NODULE)
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

    @Override
    public GraphicReportVO queryGraphicReport(String computeSeriesId, Boolean reset) {
        GraphicReportVO graphicReportVO;
        boolean resetReport = BooleanUtil.isTrue(reset);
        if (resetReport) {
            graphicReportMapper.delete(Wrappers.<GraphicReportPO>lambdaQuery()
                    .eq(GraphicReportPO::getReportType, DiagnosisType.NODULE)
                    .eq(GraphicReportPO::getComputeSeriesId, computeSeriesId));
            graphicReportVO = generateAndSaveGraphicReport(computeSeriesId);
        } else {
            GraphicReportPO graphicReportPO = graphicReportMapper.selectOne(Wrappers.<GraphicReportPO>lambdaQuery()
                    .eq(GraphicReportPO::getReportType, DiagnosisType.NODULE)
                    .eq(GraphicReportPO::getComputeSeriesId, computeSeriesId));
            if (graphicReportPO != null) {
                graphicReportVO = BeanUtil.copyProperties(graphicReportPO, GraphicReportVO.class);
            } else {
                graphicReportVO = generateAndSaveGraphicReport(computeSeriesId);
            }
        }
        List<NoduleLesionPO> noduleLesionPOList = noduleLesionMapper.selectList(Wrappers.<NoduleLesionPO>lambdaQuery()
                .eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId)
                .eq(NoduleLesionPO::getDataType, 0)
                .eq(NoduleLesionPO::getChecked, 1));
        List<NoduleLesionVO> noduleLesionVOList = BeanUtil.copyToList(noduleLesionPOList, NoduleLesionVO.class);
        for (NoduleLesionVO noduleLesionVO : noduleLesionVOList) {
            noduleLesionVO.setLobeSegmentSort(LOBE_SEGMENT_SORT_MAP.get(noduleLesionVO.getLobeSegment()));
            noduleLesionVO.setTypeSort(TYPE_SORT_MAP.get(noduleLesionVO.getType()));
        }
        graphicReportVO.setNoduleLesionList(noduleLesionVOList);
        return graphicReportVO;
    }

    @Override
    public void updateGraphicReport(GraphicReportVO graphicReportVO) {
        graphicReportMapper.updateById(BeanUtil.copyProperties(graphicReportVO, GraphicReportPO.class));
    }

    /**
     * 对结节操作进行校验
     *
     * @param noduleOperateVO noduleOperateVO
     * @author zhuhs 2024/08/21
     */
    private void validateNoduleOperate(NoduleOperateVO noduleOperateVO) {
        List<String> majorAxisScopeFilterList = StrUtil.split(noduleOperateVO.getMajorAxisScopeFilter(), StrPool.COMMA, false, false);
        if (CollUtil.isNotEmpty(majorAxisScopeFilterList)) {
            AppAssert.isTrue(majorAxisScopeFilterList.size() == 2, "病变长径范围不正确");
            String min = majorAxisScopeFilterList.get(0);
            String max = majorAxisScopeFilterList.get(1);
            AppAssert.isTrue(NumberUtil.isDouble(min), "病变长径范围最小值不正确");
            AppAssert.isTrue(NumberUtil.isDouble(max), "病变长径范围最大值不正确");
            AppAssert.isTrue(Double.parseDouble(min) <= Double.parseDouble(max), "病变长径范围不正确");
        }
    }

    private TextReportVO generateAndSaveTextReport(String computeSeriesId) {
        ReportCommon reportCommon = queryReportCommon(computeSeriesId);
        TextReportVO textReportVO = BeanUtil.copyProperties(reportCommon, TextReportVO.class);

        TextReportPO reportPO = BeanUtil.copyProperties(textReportVO, TextReportPO.class);
        reportPO.setReportType(DiagnosisType.NODULE);
        textReportMapper.insert(reportPO);
        textReportVO.setId(reportPO.getId());

        return textReportVO;
    }

    private GraphicReportVO generateAndSaveGraphicReport(String computeSeriesId) {
        ReportCommon reportCommon = queryReportCommon(computeSeriesId);
        GraphicReportVO graphicReportVO = BeanUtil.copyProperties(reportCommon, GraphicReportVO.class);

        GraphicReportPO reportPO = BeanUtil.copyProperties(graphicReportVO, GraphicReportPO.class);
        reportPO.setReportType(DiagnosisType.NODULE);
        graphicReportMapper.insert(reportPO);
        graphicReportVO.setId(reportPO.getId());
        return graphicReportVO;
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
                .eq(ManualDiagnosisPO::getType, DiagnosisType.NODULE)
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
     * 清空现有结节影像报告
     *
     * @param computeSeriesId 计算序列ID
     * @author zhuhs 2024/08/22
     */
    private void clearNoduleReport(String computeSeriesId) {
        textReportMapper.delete(Wrappers.<TextReportPO>lambdaQuery()
                .eq(TextReportPO::getComputeSeriesId, computeSeriesId)
                .eq(TextReportPO::getReportType, DiagnosisType.NODULE));
        graphicReportMapper.delete(Wrappers.<GraphicReportPO>lambdaQuery()
                .eq(GraphicReportPO::getReportType, DiagnosisType.NODULE)
                .eq(GraphicReportPO::getComputeSeriesId, computeSeriesId));
    }
}
