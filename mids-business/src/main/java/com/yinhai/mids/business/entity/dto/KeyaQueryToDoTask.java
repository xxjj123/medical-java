package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
@Data
public class KeyaQueryToDoTask {

    /**
     * 查询任务ID
     */
    @Schema(description = "查询任务ID")
    private String queryTaskId;

    /**
     * 申请任务ID
     */
    @Schema(description = "申请任务ID")
    private String applyTaskId;

    /**
     * 计算序列ID
     */
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

    /**
     * 申请编号
     */
    @Schema(description = "申请编号")
    private String applyId;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private Date applyTime;

    /**
     * 图像数量
     */
    @Schema(description = "图像数量")
    private Integer imageCount;
}
