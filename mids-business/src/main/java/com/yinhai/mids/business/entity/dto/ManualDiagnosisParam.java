package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuhs
 * @date 2024/8/22
 */
@Data
public class ManualDiagnosisParam {

    /**
     * 计算序列ID
     */
    @NotBlank(message = "计算序列ID不能为空")
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

    /**
     * 诊断结果
     */
    @Schema(description = "诊断结果")
    private String diagnosis;

    /**
     * 诊断发现
     */
    @Schema(description = "诊断发现")
    private String finding;

}
