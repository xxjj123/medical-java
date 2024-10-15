package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 科亚AI分析结果查询任务
 *
 * @author zhuhs
 * @date 2024/10/16
 */
@Schema(description = "科亚AI分析结果查询任务")
@Data
@TableName(value = "tb_keya_query_task")
public class KeyaQueryTaskPO {
    /**
     * 查询任务ID
     */
    @TableId(value = "query_task_id", type = IdType.ASSIGN_ID)
    @Schema(description = "查询任务ID")
    private String queryTaskId;

    /**
     * 计算任务ID
     */
    @TableField(value = "compute_task_id")
    @Schema(description = "计算任务ID")
    private String computeTaskId;

    /**
     * 申请任务ID
     */
    @TableField(value = "apply_task_id")
    @Schema(description = "申请任务ID")
    private String applyTaskId;

    /**
     * 申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 任务状态 0:等待发起 1:完成 -1:异常
     */
    @TableField(value = "task_status")
    @Schema(description = "任务状态 0:等待发起 1:完成 -1:异常")
    private Integer taskStatus;

    /**
     * 申请返回
     */
    @TableField(value = "query_response")
    @Schema(description = "申请返回")
    private String queryResponse;

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