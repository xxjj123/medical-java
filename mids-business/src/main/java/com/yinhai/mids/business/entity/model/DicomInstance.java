package com.yinhai.mids.business.entity.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuhs
 * @date 2024/10/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DicomInstance extends DicomInfo {

    /**
     * 案例ID
     */
    @Schema(description = "案例ID")
    private String caseId;

    /**
     * 检查ID
     */
    @Schema(description = "检查ID")
    private String studyId;

    /**
     * 序列ID
     */
    @Schema(description = "序列ID")
    private String seriesId;

    /**
     * 实例ID
     */
    @Schema(description = "实例ID")
    private String instanceId;
}
