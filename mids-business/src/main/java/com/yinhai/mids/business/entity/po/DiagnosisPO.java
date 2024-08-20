package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 诊断
 *
 * @author zhuhs
 * @date 2024/8/20
 */
@Schema(description = "诊断")
@Data
@TableName(value = "tb_diagnosis")
public class DiagnosisPO {
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
     * 类型 calcium, pneumonia, nodule, frac
     */
    @TableField(value = "`type`")
    @Schema(description = "类型 calcium, pneumonia, nodule, frac")
    private String type;

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