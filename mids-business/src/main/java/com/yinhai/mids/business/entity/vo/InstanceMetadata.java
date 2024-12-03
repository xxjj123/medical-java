package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/11/7
 */
@Data
public class InstanceMetadata {
    /**
     * 实例ID
     */
    @Schema(description = "实例ID")
    private String instanceId;

    /**
     * InstanceNumber 实例顺序
     */
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * slicePosition
     */
    @Schema(description = "slicePosition")
    private String slicePosition;

    /**
     * view index
     */
    @Schema(description = "view index")
    private Integer viewIndex;
}