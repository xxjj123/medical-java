package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/10/17
 */
@Data
public class LungTaskInfo {
    /**
     * 申请任务状态 0:等待发起 1:等待推送 2:完成 -1:异常
     */
    @Schema(description = "申请任务状态 0:等待发起 1:等待推送 2:完成 -1:异常")
    private Integer applyTaskStatus;

    /**
     * 申请结果 0:失败 1:成功
     */
    @Schema(description = "申请结果 0:失败 1:成功")
    private Integer applyResult;

    /**
     * 推送结果 0:失败 1:成功
     */
    @Schema(description = "推送结果 0:失败 1:成功")
    private Integer pushResult;

    /**
     * 查询任务状态 0:等待发起 1:完成 -1:异常
     */
    @Schema(description = "查询任务状态 0:等待发起 1:完成 -1:异常")
    private Integer queryTaskStatus;

    /**
     * 查询结果 0:失败 1:成功
     */
    @Schema(description = "查询结果 0:失败 1:成功")
    private Integer queryResult;

    /**
     * MPR任务状态 0:等待发起 1:等待推送 2:完成 -1:异常
     */
    @Schema(description = "MPR任务状态 0:等待发起 1:等待推送 2:完成 -1:异常")
    private Integer mprTaskStatus;

    /**
     * MPR结果 0:失败 1:成功
     */
    @Schema(description = "MPR结果 0:失败 1:成功")
    private Integer mprResult;
}
