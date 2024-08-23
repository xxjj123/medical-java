package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.NoduleLesionVO;
import com.yinhai.mids.business.entity.vo.NoduleOperateVO;
import com.yinhai.mids.business.entity.vo.NoduleVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;

/**
 * @author zhuhs
 * @date 2024/8/22
 */
public interface NoduleService {

    /**
     * 结节操作查询
     *
     * @param computeSeriesId computeSeriesId
     * @return {@link NoduleOperateVO }
     * @author zhuhs 2024/08/21
     */
    NoduleOperateVO queryNoduleOperate(String computeSeriesId);

    /**
     * 结节操作保存
     *
     * @param noduleOperateVO noduleOperateVO
     * @author zhuhs 2024/08/22
     */
    void saveNoduleOperate(NoduleOperateVO noduleOperateVO);

    /**
     * 结节操作重置
     *
     * @param computeSeriesId computeSeriesId
     * @author zhuhs 2024/08/22
     */
    void resetNoduleOperate(String computeSeriesId);

    /**
     * 结节病变列表查询
     *
     * @param computeSeriesId computeSeriesId
     * @return {@link NoduleVO }
     * @author zhuhs 2024/08/21
     */
    NoduleVO queryNodule(String computeSeriesId);

    /**
     * 更新结节病变信息
     *
     * @param noduleLesionVO noduleLesionVO
     * @author zhuhs 2024/08/22
     */
    void updateNoduleLesion(NoduleLesionVO noduleLesionVO);

    /**
     * 保存人工诊断结果
     *
     * @param manualDiagnosisParam manualDiagnosisParam
     * @author zhuhs 2024/08/22
     */
    void saveNoduleManualDiagnosis(ManualDiagnosisParam manualDiagnosisParam);

    /**
     * 查询文本报告
     *
     * @param computeSeriesId computeSeriesId
     * @param reset           是否重置
     * @return {@link TextReportVO }
     * @author zhuhs 2024/08/22
     */
    TextReportVO queryTextReport(String computeSeriesId, Boolean reset);

    /**
     * 更新文本报告
     *
     * @param textReportVO textReportVO
     * @author zhuhs 2024/08/22
     */
    void updateTextReport(TextReportVO textReportVO);
}
