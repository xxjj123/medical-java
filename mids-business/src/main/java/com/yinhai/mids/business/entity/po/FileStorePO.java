package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 存储表
 *
 * @author zhuhs
 * @date 2024/7/1 16:23
 */
@Data
@TableName(value = "tb_file_store")
public class FileStorePO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 文件大小，单位字节
     */
    @TableField(value = "`size`")
    private Long size;

    /**
     * MIME类型
     */
    @TableField(value = "content_type")
    private String contentType;

    /**
     * 原始名称
     */
    @TableField(value = "original_name")
    private String originalName;

    /**
     * 访问路径
     */
    @TableField(value = "access_path")
    private String accessPath;

    /**
     * 文件上传时间
     */
    @TableField(value = "upload_time")
    private Date uploadTime;

    /**
     * 数据创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 数据修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}