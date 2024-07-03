package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 实例表
 *
 * @author zhuhs
 * @date 2024/7/2 10:43
 */
@Schema(description = "实例表")
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
     * StudyInstanceUID 研究实例UID
     */
    @TableField(value = "study_uid")
    @Schema(description = "StudyInstanceUID 研究实例UID")
    private String studyUid;

    /**
     * SeriesInstanceUID 序列实例UID
     */
    @TableField(value = "series_uid")
    @Schema(description = "SeriesInstanceUID 序列实例UID")
    private String seriesUid;

    /**
     * SOPInstanceUID 服务对象对实例UID
     */
    @TableField(value = "instance_uid")
    @Schema(description = "SOPInstanceUID 服务对象对实例UID")
    private String instanceUid;

    /**
     * InstanceNumber 实例顺序
     */
    @TableField(value = "instance_number")
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * 逻辑删除
     */
    @TableField(value = "deleted")
    @Schema(description = "逻辑删除")
    private Integer deleted;

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