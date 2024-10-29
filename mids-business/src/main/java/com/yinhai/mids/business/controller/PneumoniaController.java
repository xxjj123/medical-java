package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.PneumoniaVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;
import com.yinhai.mids.business.service.PneumoniaService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhuhs
 * @date 2024/10/28
 */
@Validated
@Tag(name = "肺炎病变诊断")
@RestService("pneumonia")
public class PneumoniaController {

    @Resource
    private PneumoniaService pneumoniaService;

    @Operation(summary = "重置所有肺炎操作")
    @PostMapping("resetOperate")
    public void resetOperate(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        pneumoniaService.resetPneumoniaOperate(computeSeriesId);
    }

    @Operation(summary = "肺炎病变列表查询")
    @PostMapping("queryPneumonia")
    public PneumoniaVO queryPneumonia(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return pneumoniaService.queryPneumonia(computeSeriesId);
    }

    @Operation(summary = "更新肺炎病变选中状态")
    @PostMapping("updateCheckStatus")
    public void updateCheckStatus(@RequestParam @NotBlank(message = "病变ID不能为空") String pneumoniaLesionId,
                                  @RequestParam @NotNull(message = "选中状态不能为空") Boolean checked) {
        pneumoniaService.updateCheckStatus(pneumoniaLesionId, checked);
    }

    @Operation(summary = "保存人工诊断结果")
    @PostMapping("saveManualDiagnosis")
    public void saveManualDiagnosis(@RequestBody @Validated ManualDiagnosisParam manualDiagnosisParam) {
        pneumoniaService.savePneumoniaManualDiagnosis(manualDiagnosisParam);
    }

    @Operation(summary = "查询文本报告")
    @PostMapping("queryTextReport")
    public TextReportVO queryTextReport(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId,
                                        @RequestParam(required = false) Boolean reset) {
        return pneumoniaService.queryTextReport(computeSeriesId, reset);
    }

    @Operation(summary = "更新文本报告")
    @PostMapping("updateTextReport")
    public void saveTextReport(@RequestBody @Validated TextReportVO textReportVO) {
        pneumoniaService.updateTextReport(textReportVO);
    }
}
