package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 肺炎病变
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Schema(description = "肺炎病变")
@Data
@TableName(value = "tb_pneumonia_lesion")
public class PneumoniaLesionPO {
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
     * 肺叶名称
     */
    @TableField(value = "lobe_name")
    @Schema(description = "肺叶名称")
    private String lobeName;

    /**
     * 肺叶体积
     */
    @TableField(value = "lobe_volume")
    @Schema(description = "肺叶体积")
    private Double lobeVolume;

    /**
     * 病变体积
     */
    @TableField(value = "disease_volume")
    @Schema(description = "病变体积")
    private Double diseaseVolume;

    /**
     * 病变平均HU值
     */
    @TableField(value = "intensity")
    @Schema(description = "病变平均HU值")
    private Double intensity;

    /**
     * 病变类型 GGO/HD
     */
    @TableField(value = "disease_class")
    @Schema(description = "病变类型 GGO/HD")
    private String diseaseClass;

    /**
     * 数据创建时间
     */
    @TableField(value = "create_time")
    @Schema(description = "数据创建时间")
    private Date createTime;

    /**
     * 数据修改时间
     */
    @TableField(value = "update_time")
    @Schema(description = "数据修改时间")
    private Date updateTime;
}