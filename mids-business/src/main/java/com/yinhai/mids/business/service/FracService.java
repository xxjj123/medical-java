package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.FracLesionVO;
import com.yinhai.mids.business.entity.vo.FracVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;

/**
 * @author zhuhs
 * @date 2024/10/29
 */
public interface FracService {

    /**
     * 重置所有骨折操作
     */
    void resetFracOperate(String computeSeriesId);

    /**
     * 骨折病变列表查询
     */
    FracVO queryFrac(String computeSeriesId);

    /**
     * 更新骨折病变信息
     */
    void updateFracLesion(FracLesionVO fracLesionVO);

    /**
     * 保存人工诊断结果
     */
    void saveFracManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam);

    /**
     * 查询文本报告
     */
    TextReportVO queryTextReport(String computeSeriesId, Boolean reset);

    /**
     * 更新文本报告
     */
    void updateTextReport(TextReportVO textReportVO);
}
