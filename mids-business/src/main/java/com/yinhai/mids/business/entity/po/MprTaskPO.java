package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * MPR任务
 *
 * @author zhuhs
 * @date 2024/10/21
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
     * 计算序列ID
     */
    @TableField(value = "compute_series_id")
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

    /**
     * 任务状态 0:等待发起 1:等待推送 2:完成 -1:异常
     */
    @TableField(value = "task_status")
    @Schema(description = "任务状态 0:等待发起 1:等待推送 2:完成 -1:异常")
    private Integer taskStatus;

    /**
     * MPR类型
     */
    @TableField(value = "mpr_type")
    @Schema(description = "MPR类型")
    private String mprType;

    /**
     * 申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 发起时间
     */
    @TableField(value = "mpr_time")
    @Schema(description = "发起时间")
    private Date mprTime;

    /**
     * MPR返回
     */
    @TableField(value = "mpr_response")
    @Schema(description = "MPR返回")
    private String mprResponse;

    /**
     * MPR结果 0:失败 1:成功
     */
    @TableField(value = "mpr_result")
    @Schema(description = "MPR结果 0:失败 1:成功")
    private Integer mprResult;

    /**
     * 推送时间
     */
    @TableField(value = "push_time")
    @Schema(description = "推送时间")
    private Date pushTime;

    /**
     * 推送内容
     */
    @TableField(value = "push_content")
    @Schema(description = "推送内容")
    private String pushContent;

    /**
     * 推送结果 0:失败 1:成功
     */
    @TableField(value = "push_result")
    @Schema(description = "推送结果 0:失败 1:成功")
    private Integer pushResult;

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