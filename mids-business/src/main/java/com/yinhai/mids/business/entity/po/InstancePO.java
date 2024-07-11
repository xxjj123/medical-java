package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 实例
 *
 * @author zhuhs
 * @date 2024/7/11 9:21
 */
@Schema(description = "实例")
@Data
@TableName(value = "tb_instance")
public class InstancePO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_study.id
     */
    @TableField(value = "study_id")
    @Schema(description = "tb_study.id")
    private String studyId;

    /**
     * tb_series.id
     */
    @TableField(value = "series_id")
    @Schema(description = "tb_series.id")
    private String seriesId;

    /**
     * StudyInstanceUID
     */
    @TableField(value = "study_instance_uid")
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * SeriesInstanceUID
     */
    @TableField(value = "series_instance_uid")
    @Schema(description = "SeriesInstanceUID")
    private String seriesInstanceUid;

    /**
     * SOPInstanceUID
     */
    @TableField(value = "sop_instance_uid")
    @Schema(description = "SOPInstanceUID")
    private String sopInstanceUid;

    /**
     * InstanceNumber 实例顺序
     */
    @TableField(value = "instance_number")
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * 访问路径
     */
    @TableField(value = "access_path")
    @Schema(description = "访问路径")
    private String accessPath;

    /**
     * 逻辑删除
     */
    @TableField(value = "deleted")
    @Schema(description = "逻辑删除")
    private Boolean deleted;

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