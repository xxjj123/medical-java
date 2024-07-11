package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 分析概述
 *
 * @author zhuhs
 * @date 2024/7/9 17:28
 */
@Schema(description = "分析概述")
@Data
@TableName(value = "tb_analysis_overview")
public class AnalysisOverviewPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * 申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 类型 calcium, pneumonia, nodule, frac
     */
    @TableField(value = "`type`")
    @Schema(description = "类型 calcium, pneumonia, nodule, frac")
    private String type;

    /**
     * SeriesInstanceUID 序列实例UID
     */
    @TableField(value = "series_uid")
    @Schema(description = "SeriesInstanceUID 序列实例UID")
    private String seriesUid;

    /**
     * 诊断结果
     */
    @TableField(value = "diagnosis")
    @Schema(description = "诊断结果")
    private String diagnosis;

    /**
     * 诊断发现
     */
    @TableField(value = "finding")
    @Schema(description = "诊断发现")
    private String finding;

    /**
     * 阴阳性
     */
    @TableField(value = "has_lesion")
    @Schema(description = "阴阳性")
    private Boolean hasLesion;

    /**
     * 病灶数量
     */
    @TableField(value = "`number`")
    @Schema(description = "病灶数量")
    private Integer number;

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