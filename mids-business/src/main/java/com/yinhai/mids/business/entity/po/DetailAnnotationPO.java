package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 框选区域数据集合
 *
 * @author zhuhs
 * @date 2024/7/9 17:28
 */
@Schema(description = "框选区域数据集合")
@Data
@TableName(value = "tb_detail_annotation")
public class DetailAnnotationPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * 病灶详情ID
     */
    @TableField(value = "volume_detail_id")
    @Schema(description = "病灶详情ID")
    private String volumeDetailId;

    /**
     * 申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 同属一个annotation标识
     */
    @TableField(value = "annotation_uid")
    @Schema(description = "同属一个annotation标识")
    private String annotationUid;

    /**
     * 标识类型 CUBE: ⻓⽅体空间坐标
     */
    @TableField(value = "`type`")
    @Schema(description = "标识类型 CUBE: ⻓⽅体空间坐标")
    private String type;

    /**
     * 影像坐标系i
     */
    @TableField(value = "point_x")
    @Schema(description = "影像坐标系i")
    private Integer pointX;

    /**
     * 影像坐标系j
     */
    @TableField(value = "point_y")
    @Schema(description = "影像坐标系j")
    private Integer pointY;

    /**
     * InstanceNumber
     */
    @TableField(value = "point_z")
    @Schema(description = "InstanceNumber")
    private Integer pointZ;

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