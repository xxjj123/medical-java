package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 科亚AI分析申请任务
 *
 * @author zhuhs
 * @date 2024/10/16
 */
@Schema(description = "科亚AI分析申请任务")
@Data
@TableName(value = "tb_keya_apply_task")
public class KeyaApplyTaskPO {
    /**
     * 申请任务ID
     */
    @TableId(value = "apply_task_id", type = IdType.ASSIGN_ID)
    @Schema(description = "申请任务ID")
    private String applyTaskId;

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
     * 申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 发起时间
     */
    @TableField(value = "apply_time")
    @Schema(description = "发起时间")
    private Date applyTime;

    /**
     * 推送时间
     */
    @TableField(value = "push_time")
    @Schema(description = "推送时间")
    private Date pushTime;

    /**
     * 申请返回
     */
    @TableField(value = "apply_response")
    @Schema(description = "申请返回")
    private String applyResponse;

    /**
     * 推送返回
     */
    @TableField(value = "push_response")
    @Schema(description = "推送返回")
    private String pushResponse;

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