package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 骨折病变
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Schema(description = "骨折病变")
@Data
@TableName(value = "tb_frac_lesion")
public class FracLesionPO {
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
     * 选中状态
     */
    @TableField(value = "`checked`")
    @Schema(description = "选中状态")
    private Boolean checked;

    /**
     * 骨折所在的边：左侧、右侧
     */
    @TableField(value = "rib_side")
    @Schema(description = "骨折所在的边：左侧、右侧")
    private String ribSide;

    /**
     * 骨折位置：front(前段)/middle(腋段)/back(后段)
     */
    @TableField(value = "rib_type")
    @Schema(description = "骨折位置：front(前段)/middle(腋段)/back(后段)")
    private String ribType;

    /**
     * 骨折所在侧的肋骨编号
     */
    @TableField(value = "rib_num")
    @Schema(description = "骨折所在侧的肋骨编号")
    private Integer ribNum;

    /**
     * 骨折类型：old(陈旧性骨折)/buckle(骨皮质扭曲)/displaced(错位性骨折)/nodisplaced(非错位性骨折)
     */
    @TableField(value = "frac_class")
    @Schema(description = "骨折类型：old(陈旧性骨折)/buckle(骨皮质扭曲)/displaced(错位性骨折)/nodisplaced(非错位性骨折)")
    private String fracClass;

    /**
     * 骨折区域的bbox
     */
    @TableField(value = "frac_b_box")
    @Schema(description = "骨折区域的bbox")
    private String fracBBox;

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