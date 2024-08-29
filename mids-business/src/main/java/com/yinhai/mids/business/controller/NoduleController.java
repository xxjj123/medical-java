package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.*;
import com.yinhai.mids.business.service.NoduleService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * @author zhuhs
 * @date 2024/8/22
 */
@Validated
@Tag(name = "结节病变诊断")
@RestService("nodule")
public class NoduleController {

    @Resource
    private NoduleService noduleService;

    @Operation(summary = "查询上次结节操作")
    @PostMapping("queryOperate")
    public NoduleOperateVO queryOperate(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return noduleService.queryNoduleOperate(computeSeriesId);
    }

    @Operation(summary = "保存本次结节操作")
    @PostMapping("saveOperate")
    public void saveOperate(@RequestBody @Validated NoduleOperateVO noduleOperateVO) {
        noduleService.saveNoduleOperate(noduleOperateVO);
    }

    @Operation(summary = "重置所有结节操作")
    @PostMapping("resetOperate")
    public void resetOperate(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        noduleService.resetNoduleOperate(computeSeriesId);
    }

    @Operation(summary = "结节病变列表查询")
    @PostMapping("queryNodule")
    public NoduleVO queryNodule(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return noduleService.queryNodule(computeSeriesId);
    }

    @Operation(summary = "更新结节病变信息")
    @PostMapping("updateNoduleLesion")
    public void updateNoduleLesion(@RequestBody @Validated NoduleLesionVO noduleLesionVO) {
        noduleService.updateNoduleLesion(noduleLesionVO);
    }

    @Operation(summary = "保存人工诊断结果")
    @PostMapping("saveManualDiagnosis")
    public void saveManualDiagnosis(@RequestBody @Validated ManualDiagnosisParam manualDiagnosisParam) {
        noduleService.saveNoduleManualDiagnosis(manualDiagnosisParam);
    }

    @Operation(summary = "查询文本报告")
    @PostMapping("queryTextReport")
    public TextReportVO queryTextReport(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId,
                                        @RequestParam(required = false) Boolean reset) {
        return noduleService.queryTextReport(computeSeriesId, reset);
    }

    @Operation(summary = "更新文本报告")
    @PostMapping("updateTextReport")
    public void saveTextReport(@RequestBody @Validated TextReportVO textReportVO) {
        noduleService.updateTextReport(textReportVO);
    }

    @Operation(summary = "查询图文报告")
    @PostMapping("queryGraphicReport")
    public GraphicReportVO queryGraphicReport(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId,
                                        @RequestParam(required = false) Boolean reset) {
        return noduleService.queryGraphicReport(computeSeriesId, reset);
    }

    @Operation(summary = "更新图文报告")
    @PostMapping("updateGraphicReport")
    public void updateGraphicReport(@RequestBody @Validated GraphicReportVO graphicReportVO) {
        noduleService.updateGraphicReport(graphicReportVO);
    }
}
