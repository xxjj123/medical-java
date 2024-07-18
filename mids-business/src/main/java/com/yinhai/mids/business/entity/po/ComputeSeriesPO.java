package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 序列计算
 *
 * @author zhuhs
 * @date 2024/7/11 9:22
 */
@Accessors(chain = true)
@Schema(description = "序列计算")
@Data
@TableName(value = "tb_compute_series")
public class ComputeSeriesPO {
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
     * 算法类型
     */
    @TableField(value = "algorithm_type")
    @Schema(description = "算法类型")
    private String algorithmType;

    /**
     * 计算申请编号
     */
    @TableField(value = "apply_id")
    @Schema(description = "计算申请编号")
    private String applyId;

    /**
     * 计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常
     */
    @TableField(value = "compute_status")
    @Schema(description = "计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常")
    private String computeStatus;

    /**
     * 发起计算时间
     */
    @TableField(value = "compute_start_time")
    @Schema(description = "发起计算时间")
    private Date computeStartTime;

    /**
     * 计算返回结果
     */
    @TableField(value = "compute_response")
    @Schema(description = "计算返回结果")
    private String computeResponse;

    /**
     * 异常信息
     */
    @TableField(value = "error_message")
    @Schema(description = "异常信息")
    private String errorMessage;

    /**
     * 操作状态
     */
    @TableField(value = "operate_status")
    @Schema(description = "操作状态")
    private String operateStatus;

    /**
     * 操作医师
     */
    @TableField(value = "operate_doctor_user_id")
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "deleted")
    @Schema(description = "逻辑删除")
    private Boolean deleted;

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