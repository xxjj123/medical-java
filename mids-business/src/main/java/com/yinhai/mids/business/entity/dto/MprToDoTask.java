package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/10/21
 */
@Data
public class MprToDoTask {

    /**
     * 申请编号
     */
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 序列ID
     */
    @Schema(description = "序列ID")
    private String seriesId;

    /**
     * MPR类型，多个用英文逗号拼接
     */
    @Schema(description = "MPR类型，多个用英文逗号拼接")
    private String mprTypes;

    /**
     * 计算序列ID，多个用英文逗号拼接
     */
    @Schema(description = "计算序列ID，多个用英文逗号拼接")
    private String computeSeriesIds;
}
