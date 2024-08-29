package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 结节操作
 *
 * @author zhuhs
 * @date 2024/8/21
 */
@Schema(description = "结节操作")
@Data
@TableName(value = "tb_nodule_operate")
public class NoduleOperatePO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_compute_series.id
     */
    @TableField(value = "compute_series_id")
    @Schema(description = "tb_compute_series.id")
    private String computeSeriesId;

    /**
     * 病变排序类型 1-IM 2-肺段 3-长径 4-体积 5-风险 6-类型
     */
    @TableField(value = "lesion_order_type", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "病变排序类型 1-IM 2-肺段 3-长径 4-体积 5-风险 6-类型")
    private String lesionOrderType;

    /**
     * 良恶性
     */
    @TableField(value = "risk_filter", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "良恶性")
    private String riskFilter;

    /**
     * 类型
     */
    @TableField(value = "type_filter", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "类型")
    private String typeFilter;

    /**
     * 长径选择
     */
    @TableField(value = "major_axis_select_filter", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "长径选择")
    private String majorAxisSelectFilter;

    /**
     * 长径范围
     */
    @TableField(value = "major_axis_scope_filter", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "长径范围")
    private String majorAxisScopeFilter;

    /**
     * 影像所见排序类型
     */
    @TableField(value = "finding_order_type", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "影像所见排序类型")
    private String findingOrderType;

    /**
     * 影像诊断类型
     */
    @TableField(value = "diagnosis_type", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "影像诊断类型")
    private String diagnosisType;

    /**
     * 操作时间
     */
    @TableField(value = "operate_time")
    @Schema(description = "操作时间")
    private Date operateTime;

    /**
     * 数据创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "数据创建时间")
    private Date createTime;

    /**
     * 数据修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "数据修改时间")
    private Date updateTime;
}