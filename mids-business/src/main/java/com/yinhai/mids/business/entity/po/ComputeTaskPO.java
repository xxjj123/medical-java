package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 计算任务
 *
 * @author zhuhs
 * @date 2024/10/15
 */
@Schema(description = "计算任务")
@Data
@TableName(value = "tb_compute_task")
public class ComputeTaskPO {
    /**
     * 计算任务ID
     */
    @TableId(value = "compute_task_id", type = IdType.ASSIGN_ID)
    @Schema(description = "计算任务ID")
    private String computeTaskId;

    /**
     * 案例ID
     */
    @TableField(value = "case_id")
    @Schema(description = "案例ID")
    private String caseId;

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
     * 计算类型
     */
    @TableField(value = "compute_type")
    @Schema(description = "计算类型")
    private Integer computeType;

    /**
     * 计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常
     */
    @TableField(value = "compute_status")
    @Schema(description = "计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常")
    private Integer computeStatus;

    /**
     * 操作状态
     */
    @TableField(value = "operate_status")
    @Schema(description = "操作状态")
    private Integer operateStatus;

    /**
     * 操作医师
     */
    @TableField(value = "operate_doctor_user_id")
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

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