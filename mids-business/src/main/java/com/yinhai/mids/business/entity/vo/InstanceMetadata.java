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
     * InstanceNumber 实例顺序
     */
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * slicePosition
     */
    @Schema(description = "slicePosition")
    private String slicePosition;
}
