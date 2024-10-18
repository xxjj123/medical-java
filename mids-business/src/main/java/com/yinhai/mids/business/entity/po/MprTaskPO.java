package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * MPR任务
 *
 * @author zhuhs
 * @date 2024/10/16
 */
@Schema(description = "MPR任务")
@Data
@TableName(value = "tb_mpr_task")
public class MprTaskPO {
    /**
     * MPR任务ID
     */
    @TableId(value = "mpr_task_id", type = IdType.ASSIGN_ID)
    @Schema(description = "MPR任务ID")
    private String mprTaskId;

    /**
     * 计算任务ID
     */
    @TableField(value = "compute_task_id")
    @Schema(description = "计算任务ID")
    private String computeTaskId;

    /**
     * 任务状态 0:等待发起 1:等待推送 2:完成 -1:异常
     */
    @TableField(value = "task_status")
    @Schema(description = "任务状态 0:等待发起 1:等待推送 2:完成 -1:异常")
    private Integer taskStatus;

    /**
     * 发起时间
     */
    @TableField(value = "mpr_time")
    @Schema(description = "发起时间")
    private Date mprTime;

    /**
     * 申请返回
     */
    @TableField(value = "mpr_response")
    @Schema(description = "申请返回")
    private String mprResponse;

    /**
     * MPR结果 0:失败 1:成功
     */
    @TableField(value = "mpt_result")
    @Schema(description = "MPR结果 0:失败 1:成功")
    private Integer mprResult;

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