package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 结节病变
 *
 * @author zhuhs
 * @date 2024/8/20
 */
@Schema(description = "结节病变")
@Data
@TableName(value = "tb_nodule_lesion")
public class NoduleLesionPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * 数据类型 0-原始数据 1-人工数据
     */
    @TableField(value = "data_type")
    @Schema(description = "数据类型 0-原始数据 1-人工数据")
    private Integer dataType;

    /**
     * tb_compute_series.id
     */
    @TableField(value = "compute_series_id")
    @Schema(description = "tb_compute_series.id")
    private String computeSeriesId;

    /**
     * SOPInstanceUID
     */
    @TableField(value = "sop_instance_uid")
    @Schema(description = "SOPInstanceUID")
    private String sopInstanceUid;

    /**
     * 选中状态 1是0否
     */
    @TableField(value = "checked")
    @Schema(description = "选中状态 1是0否")
    private Boolean checked;

    /**
     * IM
     */
    @TableField(value = "im")
    @Schema(description = "IM")
    private Integer im;

    /**
     * 词条信息
     */
    @TableField(value = "vocabulary_entry")
    @Schema(description = "词条信息")
    private String vocabularyEntry;

    /**
     * 结节类型
     */
    @TableField(value = "`type`")
    @Schema(description = "结节类型")
    private String type;

    /**
     * 肺段
     */
    @TableField(value = "lobe_segment")
    @Schema(description = "肺段")
    private Integer lobeSegment;

    /**
     * 肺叶
     */
    @TableField(value = "lobe")
    @Schema(description = "肺叶")
    private String lobe;

    /**
     * 体积
     */
    @TableField(value = "volume")
    @Schema(description = "体积")
    private Double volume;

    /**
     * 风险
     */
    @TableField(value = "risk_code")
    @Schema(description = "风险")
    private Integer riskCode;

    /**
     * CT措施 平均HU
     */
    @TableField(value = "ct_measures_mean")
    @Schema(description = "CT措施 平均HU")
    private Double ctMeasuresMean;

    /**
     * CT措施 最小HU
     */
    @TableField(value = "ct_measures_minimum")
    @Schema(description = "CT措施 最小HU")
    private Double ctMeasuresMinimum;

    /**
     * CT措施 最大HU
     */
    @TableField(value = "ct_measures_maximum")
    @Schema(description = "CT措施 最大HU")
    private Double ctMeasuresMaximum;

    /**
     * 病灶椭圆轴 短轴⻓度
     */
    @TableField(value = "ellipsoid_axis_least")
    @Schema(description = "病灶椭圆轴 短轴⻓度")
    private Double ellipsoidAxisLeast;

    /**
     * 病灶椭圆轴 最⼩轴⻓度
     */
    @TableField(value = "ellipsoid_axis_minor")
    @Schema(description = "病灶椭圆轴 最⼩轴⻓度")
    private Double ellipsoidAxisMinor;

    /**
     * 病灶椭圆轴 ⻓轴⻓度
     */
    @TableField(value = "ellipsoid_axis_major")
    @Schema(description = "病灶椭圆轴 ⻓轴⻓度")
    private Double ellipsoidAxisMajor;

    /**
     * 坐标点
     */
    @TableField(value = "points")
    @Schema(description = "坐标点")
    private String points;

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