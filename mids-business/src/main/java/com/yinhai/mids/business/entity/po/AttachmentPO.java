package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 附件表
 *
 * @author zhuhs
 * @date 2024/7/2 10:50
 */
@Schema(description = "附件表")
@Data
@TableName(value = "tb_attachment")
public class AttachmentPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_store.id
     */
    @TableField(value = "store_id")
    @Schema(description = "tb_store.id")
    private String storeId;

    /**
     * 文件所属对象id
     */
    @TableField(value = "object_id")
    @Schema(description = "文件所属对象id")
    private String objectId;

    /**
     * 文件用途，如用户头像、首屏轮播图等
     */
    @TableField(value = "use_type")
    @Schema(description = "文件用途，如用户头像、首屏轮播图等")
    private Integer useType;

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