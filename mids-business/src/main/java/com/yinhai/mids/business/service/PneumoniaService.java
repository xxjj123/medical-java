package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.PneumoniaVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;

/**
 * @author zhuhs
 * @date 2024/10/28
 */
public interface PneumoniaService {

    /**
     * 肺炎操作重置
     */
    void resetPneumoniaOperate(String computeSeriesId);

    /**
     * 肺炎病变列表查询
     */
    PneumoniaVO queryPneumonia(String computeSeriesId);

    /**
     * 修改选中状态
     */
    void updateCheckStatus(String pneumoniaLesionId, Boolean checked);

    /**
     * 保存人工诊断结果
     */
    void savePneumoniaManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam);

    /**
     * 查询文本报告
     */
    TextReportVO queryTextReport(String computeSeriesId, Boolean reset);

    /**
     * 更新文本报告
     */
    void updateTextReport(TextReportVO textReportVO);
}
