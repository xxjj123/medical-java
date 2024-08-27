package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    @Length(max = 2000, message = "诊断结果不能超过2000字")
    @Schema(description = "诊断结果")
    private String diagnosis;

    /**
     * 诊断发现
     */
    @Length(max = 2000, message = "诊断发现不能超过2000字")
    @Schema(description = "诊断发现")
    private String finding;

}
