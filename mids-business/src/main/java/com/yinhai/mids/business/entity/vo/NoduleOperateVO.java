package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuhs
 * @date 2024/8/21
 */
@Data
public class NoduleOperateVO {
    /**
     * 计算序列ID
     */
    @Schema(description = "计算序列ID")
    @NotBlank(message = "序列ID不能为空")
    private String computeSeriesId;
    /**
     * 病变排序类型 1-IM 2-肺段 3-长径 4-体积 5-风险 6-类型
     */
    @Schema(description = "病变排序类型 1-IM 2-肺段 3-长径 4-体积 5-风险 6-类型")
    private String lesionOrderType;
    /**
     * 良恶性 1-低危 2-中危 3-高危
     */
    @Schema(description = "良恶性 1-低危 2-中危 3-高危 【多值逗号拼接】")
    private String riskFilter;
    /**
     * 类型
     */
    @Schema(description = "类型 【多值逗号拼接】")
    private String typeFilter;
    /**
     * 长径选择
     */
    @Schema(description = "长径选择 1:0-3mm, 2:3-5mm, 3:5-8mm, 4:>8mm 【多值逗号拼接】")
    private String majorAxisSelectFilter;
    /**
     * 长径范围
     */
    @Schema(description = "长径范围 【最小值,最大值】")
    private String majorAxisScopeFilter;
    /**
     * 影像所见排序类型 1-层数 2-肺段 3-类型 4-结构化描述 5-结节汇总
     */
    @Schema(description = "影像所见排序类型 1-层数 2-肺段 3-类型 4-结构化描述 5-结节汇总 【多值逗号拼接】")
    private String findingOrderType;
    /**
     * 影像诊断类型 1-无 2-NCCN
     */
    @Schema(description = "影像诊断类型 1-无 2-NCCN")
    private String diagnosisType;
    /**
     * 结节勾选项
     */
    @Schema(description = "结节勾选项，病变ID 【多值逗号拼接】")
    private String noduleSelect;
}
