package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.dto.SpineRecogToDoTask;
import com.yinhai.mids.business.entity.vo.SpineInfoVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;

/**
 * @author zhuhs
 * @date 2024/12/2
 */
public interface SpineService {

    void recognize(SpineRecogToDoTask recogTask);

    void lockedAsyncApply(SpineRecogToDoTask recogTask);

    SpineInfoVO querySpineInfo(String computeSeriesId);

    /**
     * 保存人工诊断结果
     */
    void saveSpineManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam);

    /**
     * 查询文本报告
     *
     * @param computeSeriesId computeSeriesId
     * @param reset           是否重置
     * @return {@link TextReportVO }
     */
    TextReportVO queryTextReport(String computeSeriesId, Boolean reset);

    /**
     * 更新文本报告
     *
     * @param textReportVO textReportVO
     */
    void updateTextReport(TextReportVO textReportVO);
}
