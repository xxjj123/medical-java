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
     * 实例信息ID
     */
    @TableId(value = "instance_info_id", type = IdType.ASSIGN_ID)
    @Schema(description = "实例信息ID")
    private String instanceInfoId;

    /**
     * 检查信息ID
     */
    @TableField(value = "study_info_id")
    @Schema(description = "检查信息ID")
    private String studyInfoId;

    /**
     * 序列信息ID
     */
    @TableField(value = "series_info_id")
    @Schema(description = "序列信息ID")
    private String seriesInfoId;

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