package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 脊柱识别任务
 *
 * @author zhuhs
 * @date 2024/12/2
 */
@Schema(description = "脊柱识别任务")
@Data
@TableName(value = "tb_spine_recog_task")
public class SpineRecogTaskPO {
    /**
     * 脊柱识别任务ID
     */
    @TableId(value = "spine_recog_task_id", type = IdType.ASSIGN_ID)
    @Schema(description = "脊柱识别任务ID")
    private String spineRecogTaskId;

    /**
     * 计算序列ID
     */
    @TableField(value = "compute_series_id")
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

    /**
     * 任务状态 0:等待发起 1:完成 -1:异常
     */
    @TableField(value = "task_status")
    @Schema(description = "任务状态 0:等待发起 1:完成 -1:异常")
    private Integer taskStatus;

    /**
     * 识别返回
     */
    @TableField(value = "recog_response")
    @Schema(description = "识别返回")
    private String recogResponse;

    /**
     * 识别结果 0:失败 1:成功
     */
    @TableField(value = "recog_result")
    @Schema(description = "识别结果 0:失败 1:成功")
    private Integer recogResult;

    /**
     * 异常信息
     */
    @TableField(value = "error_message")
    @Schema(description = "异常信息")
    private String errorMessage;

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