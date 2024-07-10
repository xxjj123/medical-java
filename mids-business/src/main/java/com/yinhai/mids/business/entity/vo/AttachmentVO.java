package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2022/10/12 19:12
 */
@Data
public class AttachmentVO {

    @Schema(description = "storeId")
    private String storeId;

    /**
     * 文件所属对象id
     */
    @Schema(description = "文件所属对象id")
    private String objectId;

    /**
     * 文件用途，如用户头像、首屏轮播图等
     */
    @Schema(description = "文件用途，如用户头像、首屏轮播图等")
    private Integer useType;

    /**
     * 文件大小，单位字节
     */
    @Schema(description = "文件大小，单位字节")
    private Long size;

    /**
     * MIME类型
     */
    @Schema(description = "MIME类型")
    private String contentType;

    /**
     * 原始名称
     */
    @Schema(description = "原始名称")
    private String originalName;

    /**
     * 访问路径
     */
    @Schema(description = "访问路径")
    private String accessPath;
}
