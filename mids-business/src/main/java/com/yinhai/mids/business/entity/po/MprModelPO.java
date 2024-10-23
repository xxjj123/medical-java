package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * MPR三维模型
 *
 * @author zhuhs
 * @date 2024/10/21
 */
@Schema(description = "MPR三维模型")
@Data
@TableName(value = "tb_mpr_model")
public class MprModelPO {
    /**
     * MPR模型ID
     */
    @TableId(value = "mpr_model_id", type = IdType.ASSIGN_ID)
    @Schema(description = "MPR模型ID")
    private String mprModelId;

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
     * MPR类型
     */
    @TableField(value = "mpr_type")
    @Schema(description = "MPR类型")
    private String mprType;

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