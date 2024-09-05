package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 任务锁
 *
 * @author zhuhs
 * @date 2024/9/4
 */
@Schema(description = "任务锁")
@Data
@TableName(value = "tb_task_lock")
public class TaskLockPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * 任务对象ID
     */
    @TableField(value = "item_id")
    @Schema(description = "任务对象ID")
    private String itemId;

    /**
     * 任务类型 1-AI计算 2-查询AI计算结果
     */
    @TableField(value = "task_type")
    @Schema(description = "任务类型 1-AI计算 2-查询AI计算结果")
    private Integer taskType;

    /**
     * 是否有效 0-否 1-是
     */
    @TableField(value = "effective")
    @Schema(description = "是否有效 0-否 1-是")
    private Boolean effective;

    /**
     * 生效时间
     */
    @TableField(value = "effective_time", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "生效时间")
    private Date effectiveTime;

    /**
     * 失效时间
     */
    @TableField(value = "expire_time", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "失效时间")
    private Date expireTime;

    /**
     * 版本（用于乐观锁）
     */
    @Version
    @TableField(value = "version")
    @Schema(description = "版本（用于乐观锁）")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}