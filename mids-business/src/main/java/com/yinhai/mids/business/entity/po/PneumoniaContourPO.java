package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 肺炎轮廓
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Schema(description = "肺炎轮廓")
@Data
@TableName(value = "tb_pneumonia_contour")
public class PneumoniaContourPO {
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
     * view name
     */
    @TableField(value = "view_name")
    @Schema(description = "view name")
    private String viewName;

    /**
     * InstanceNumber 实例顺序
     */
    @TableField(value = "instance_number")
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * 轮廓点位
     */
    @TableField(value = "points")
    @Schema(description = "轮廓点位")
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