package com.yinhai.mids.business.analysis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.analysis.keya.KeyaAnalyseResult;
import com.yinhai.mids.business.analysis.keya.KeyaClient;
import com.yinhai.mids.business.analysis.keya.KeyaResponse;
import com.yinhai.mids.business.analysis.keya.RegisterBody;
import com.yinhai.mids.business.constant.AttachmentType;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.AttachmentVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.util.AttachmentKit;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yinhai.mids.business.constant.ComputeStatus.COMPUTE_ERROR;
import static com.yinhai.mids.business.constant.ComputeStatus.IN_COMPUTE;

/**
 * @author zhuhs
 * @date 2024/7/8 15:25
 */
@Component
public class AnalyseEngine {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private AnalysisOverviewMapper analysisOverviewMapper;

    @Resource
    private VolumeDetailMapper volumeDetailMapper;

    @Resource
    private DetailAnnotationMapper detailAnnotationMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource
    private KeyaClient keyaClient;

    public void register(SeriesPO seriesPO) {
        try {
            StudyPO studyPO = studyMapper.selectById(seriesPO.getStudyId());
            AppAssert.notNull(seriesPO, "检查{}不存在！", seriesPO.getStudyId());
            List<AttachmentVO> attachments = AttachmentKit.getAttachments(seriesPO.getId(), AttachmentType.DICOM_ZIP);
            AppAssert.notEmpty(attachments, "序列{}DICOM文件丢失！", seriesPO.getId());

            TaFSObject fsObject = fsManager.getObject("mids", attachments.get(0).getAccessPath());
            RegisterBody registerBody = new RegisterBody();
            String applyId = IdUtil.fastSimpleUUID();
            registerBody.setApplyId(applyId);
            registerBody.setAccessionNumber(studyPO.getAccessionNumber());
            registerBody.setStudyInstanceUID(seriesPO.getStudyUid());
            registerBody.setHospitalId("ctbay99");
            registerBody.setExaminedName("胸部平扫");
            registerBody.setPatientName(studyPO.getPatientName());
            registerBody.setPatientAge(studyPO.getPatientAge());
            registerBody.setStudyDate(DateUtil.formatDateTime(studyPO.getStudyDatetime()));
            KeyaResponse response = keyaClient.register(fsObject.getInputstream(), registerBody);
            if (response.getCode() != 1) {
                log.error("发起分析异常，分析引擎返回{}", JsonKit.toJsonString(response));
                seriesMapper.updateById(new SeriesPO().setId(seriesPO.getId()).setComputeStatus(COMPUTE_ERROR));
                return;
            }

            seriesPO.setApplyId(applyId);
            seriesMapper.updateById(new SeriesPO().setId(seriesPO.getId()).setComputeStatus(IN_COMPUTE));
        } catch (Exception e) {
            log.error(e, "发起分析异常，{}", e.getMessage());
            seriesMapper.updateById(new SeriesPO().setId(seriesPO.getId()).setComputeStatus(COMPUTE_ERROR));
        }
    }

    public void result(SeriesPO seriesPO) {
        String applyId = seriesPO.getApplyId();
        KeyaResponse keyaResponse = keyaClient.result(applyId);
        if (keyaResponse.getCode() == 1) {
            String dataContent = JsonKit.parseTree(JsonKit.toJsonString(keyaResponse)).get("data").toString();
            KeyaAnalyseResult analyseResult = JsonKit.parseObject(dataContent, KeyaAnalyseResult.class);
            AppAssert.notNull(analyseResult, "解析分析结果异常");

            List<AnalysisOverviewPO> analysisOverviewPOList = new ArrayList<>();
            KeyaAnalyseResult.Result result = analyseResult.getResult();
            CollUtil.addIfAbsent(analysisOverviewPOList, getCalciumOverviewPO(applyId, result));
            CollUtil.addIfAbsent(analysisOverviewPOList, getPneumoniaOverviewPO(applyId, result));
            AnalysisOverviewPO noduleOverviewPO = getNoduleOverviewPO(applyId, result);
            CollUtil.addIfAbsent(analysisOverviewPOList, noduleOverviewPO);
            CollUtil.addIfAbsent(analysisOverviewPOList, getFracOverviewPO(applyId, result));
            analysisOverviewMapper.insertBatch(analysisOverviewPOList);
            saveDetailAndAnnotations(applyId, result, noduleOverviewPO);
        }
    }

    private AnalysisOverviewPO getCalciumOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Calcium calcium = result.getCalcium();
        if (calcium == null) {
            return null;
        }
        AnalysisOverviewPO calciumOverview = new AnalysisOverviewPO();
        calciumOverview.setApplyId(applyId);
        calciumOverview.setType("calcium");
        calciumOverview.setSeriesUid(calcium.getSeriesInstanceUID());
        calciumOverview.setFinding(calcium.getFinding());
        calciumOverview.setHasLesion(calcium.isHasLesion());
        calciumOverview.setNumber(calcium.getNumber());
        return calciumOverview;
    }

    private AnalysisOverviewPO getPneumoniaOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Pneumonia pneumonia = result.getPneumonia();
        if (pneumonia == null) {
            return null;
        }
        AnalysisOverviewPO pneumoniaOverview = new AnalysisOverviewPO();
        pneumoniaOverview.setApplyId(applyId);
        pneumoniaOverview.setType("pneumonia");
        pneumoniaOverview.setSeriesUid(pneumonia.getSeriesInstanceUID());
        pneumoniaOverview.setDiagnosis(pneumonia.getDiagnosis());
        pneumoniaOverview.setFinding(pneumonia.getFinding());
        pneumoniaOverview.setHasLesion(pneumonia.isHasLesion());
        pneumoniaOverview.setNumber(pneumonia.getNumber());
        return pneumoniaOverview;
    }

    private AnalysisOverviewPO getNoduleOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Nodule nodule = result.getNodule();
        if (nodule == null) {
            return null;
        }
        AnalysisOverviewPO noduleOverview = new AnalysisOverviewPO();
        noduleOverview.setApplyId(applyId);
        noduleOverview.setType("nodule");
        noduleOverview.setSeriesUid(nodule.getSeriesInstanceUID());
        noduleOverview.setDiagnosis(nodule.getDiagnosis());
        noduleOverview.setFinding(nodule.getFinding());
        noduleOverview.setHasLesion(nodule.isHasLesion());
        noduleOverview.setNumber(nodule.getNumber());
        return noduleOverview;
    }

    private AnalysisOverviewPO getFracOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Frac frac = result.getFrac();
        if (frac == null) {
            return null;
        }
        AnalysisOverviewPO fracOverview = new AnalysisOverviewPO();
        fracOverview.setApplyId(applyId);
        fracOverview.setType("frac");
        fracOverview.setSeriesUid(frac.getSeriesInstanceUID());
        fracOverview.setDiagnosis(frac.getDiagnosis());
        fracOverview.setFinding(frac.getFinding());
        fracOverview.setHasLesion(frac.isHasLesion());
        fracOverview.setNumber(frac.getNumber());
        return fracOverview;
    }

    private void saveDetailAndAnnotations(String applyId, KeyaAnalyseResult.Result result, AnalysisOverviewPO noduleOverviewPO) {
        if (result.getNodule() == null) {
            return;
        }
        List<KeyaAnalyseResult.Result.Nodule.VolumeDetail> volumeDetailList = result.getNodule().getVolumeDetailList();
        if (CollUtil.isEmpty(volumeDetailList)) {
            return;
        }
        List<VolumeDetailPO> volumeDetailPOList = new ArrayList<>();
        Map<DetailAnnotationPO, VolumeDetailPO> detailAnnotationPOMap = new HashMap<>();
        for (KeyaAnalyseResult.Result.Nodule.VolumeDetail volumeDetail : volumeDetailList) {
            VolumeDetailPO volumeDetailPO = new VolumeDetailPO();
            volumeDetailPO.setOverviewId(noduleOverviewPO.getId());
            volumeDetailPO.setApplyId(applyId);
            volumeDetailPO.setSopInstanceUid(volumeDetail.getSopInstanceUID());
            volumeDetailPO.setVocabularyEntry(volumeDetail.getVocabularyEntry());
            volumeDetailPO.setType(volumeDetail.getType());
            volumeDetailPO.setLobeSegment(volumeDetail.getLobeSegment());
            volumeDetailPO.setLobe(volumeDetail.getLobe());
            volumeDetailPO.setVolume(volumeDetail.getVolume());
            volumeDetailPO.setRiskCode(volumeDetail.getRiskCode());
            KeyaAnalyseResult.Result.Nodule.VolumeDetail.CtMeasures ctMeasures = volumeDetail.getCtMeasures();
            if (ctMeasures != null) {
                volumeDetailPO.setCtMeasuresMean(ctMeasures.getMean());
                volumeDetailPO.setCtMeasuresMinimum(ctMeasures.getMinimum());
                volumeDetailPO.setCtMeasuresMaximum(ctMeasures.getMaximum());
            }
            KeyaAnalyseResult.Result.Nodule.VolumeDetail.EllipsoidAxis ellipsoidAxis = volumeDetail.getEllipsoidAxis();
            if (ellipsoidAxis != null) {
                volumeDetailPO.setEllipsoidAxisLeast(ellipsoidAxis.getLeast());
                volumeDetailPO.setEllipsoidAxisMinor(ellipsoidAxis.getMinor());
                volumeDetailPO.setEllipsoidAxisMajor(ellipsoidAxis.getMajor());
            }
            volumeDetailPOList.add(volumeDetailPO);
            List<KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation> annotationList = volumeDetail.getAnnotation();
            if (CollUtil.isEmpty(annotationList)) {
                continue;
            }
            for (KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation annotation : annotationList) {
                List<KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation.Point> points = annotation.getPoints();
                if (CollUtil.isNotEmpty(points)) {
                    String annotationUid = IdUtil.fastSimpleUUID();
                    for (KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation.Point point : points) {
                        DetailAnnotationPO annotationPO = new DetailAnnotationPO();
                        annotationPO.setApplyId(applyId);
                        annotationPO.setAnnotationUid(annotationUid);
                        annotationPO.setType(annotation.getType());
                        annotationPO.setPointX(point.getX());
                        annotationPO.setPointY(point.getY());
                        annotationPO.setPointZ(point.getZ());
                        detailAnnotationPOMap.put(annotationPO, volumeDetailPO);
                    }
                }
            }
        }
        volumeDetailMapper.insertBatch(volumeDetailPOList);
        detailAnnotationPOMap.forEach((key, value) -> key.setVolumeDetailId(value.getId()));
        detailAnnotationMapper.insertBatch(detailAnnotationPOMap.keySet());
    }

}
