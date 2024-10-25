package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 实例信息
 *
 * @author zhuhs
 * @date 2024/10/14
 */
@Schema(description = "实例信息")
@Data
@TableName(value = "tb_instance_info")
public class InstanceInfoPO {
    /**
     * 实例ID
     */
    @TableId(value = "instance_id", type = IdType.ASSIGN_ID)
    @Schema(description = "实例ID")
    private String instanceId;

    /**
     * 检查ID
     */
    @TableField(value = "study_id")
    @Schema(description = "检查ID")
    private String studyId;

    /**
     * 序列ID
     */
    @TableField(value = "series_id")
    @Schema(description = "序列ID")
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
     * view index
     */
    @TableField(value = "view_index")
    @Schema(description = "view index")
    private Integer viewIndex;

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