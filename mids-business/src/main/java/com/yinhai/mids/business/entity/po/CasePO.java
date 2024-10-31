package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 案例
 *
 * @author zhuhs
 * @date 2024/10/14
 */
@Schema(description = "案例")
@Data
@TableName(value = "tb_case")
public class CasePO {
    /**
     * 案例ID
     */
    @TableId(value = "case_id", type = IdType.ASSIGN_UUID)
    @Schema(description = "案例ID")
    private String caseId;

    /**
     * 案例创建时间
     */
    @TableField(value = "case_create_time")
    @Schema(description = "案例创建时间")
    private Date caseCreateTime;

    /**
     * 案例创建用户ID
     */
    @TableField(value = "case_create_user_id")
    @Schema(description = "案例创建用户ID")
    private String caseCreateUserId;

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