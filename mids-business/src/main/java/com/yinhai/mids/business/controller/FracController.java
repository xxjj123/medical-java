package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.dto.ManualDiagnosisParam;
import com.yinhai.mids.business.entity.vo.FracLesionVO;
import com.yinhai.mids.business.entity.vo.FracVO;
import com.yinhai.mids.business.entity.vo.TextReportVO;
import com.yinhai.mids.business.service.FracService;
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
 * @date 2024/10/28
 */
@Validated
@Tag(name = "骨折病变诊断")
@RestService("frac")
public class FracController {

    @Resource
    private FracService fracService;

    @Operation(summary = "重置所有骨折操作")
    @PostMapping("resetOperate")
    public void resetOperate(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        fracService.resetFracOperate(computeSeriesId);
    }

    @Operation(summary = "骨折病变列表查询")
    @PostMapping("queryFrac")
    public FracVO queryFrac(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return fracService.queryFrac(computeSeriesId);
    }

    @Operation(summary = "更新骨折病变信息")
    @PostMapping("updateFracLesion")
    public void updateFracLesion(@RequestBody @Validated FracLesionVO fracLesionVO) {
        fracService.updateFracLesion(fracLesionVO);
    }

    @Operation(summary = "保存人工诊断结果")
    @PostMapping("saveManualDiagnosis")
    public void saveManualDiagnosis(@RequestBody @Validated ManualDiagnosisParam manualDiagnosisParam) {
        fracService.saveFracManualDiagnosis(manualDiagnosisParam);
    }

    @Operation(summary = "查询文本报告")
    @PostMapping("queryTextReport")
    public TextReportVO queryTextReport(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId,
                                        @RequestParam(required = false) Boolean reset) {
        return fracService.queryTextReport(computeSeriesId, reset);
    }

    @Operation(summary = "更新文本报告")
    @PostMapping("updateTextReport")
    public void saveTextReport(@RequestBody @Validated TextReportVO textReportVO) {
        fracService.updateTextReport(textReportVO);
    }
}
