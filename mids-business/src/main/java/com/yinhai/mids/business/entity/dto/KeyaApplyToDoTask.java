package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
@Data
public class KeyaApplyToDoTask {

    /**
     * 申请任务ID
     */
    @Schema(description = "申请任务ID")
    private String applyTaskId;

    /**
     * 计算任务ID
     */
    @Schema(description = "计算任务ID")
    private String computeTaskId;

    /**
     * 序列信息ID
     */
    @Schema(description = "序列信息ID")
    private String seriesInfoId;

    /**
     * StudyInstanceUID 研究实例UID
     */
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * AccessionNumber 检查号
     */
    @Schema(description = "AccessionNumber 检查号")
    private String accessionNumber;

    /**
     * PatientName 患者姓名
     */
    @Schema(description = "PatientName 患者姓名")
    private String patientName;

    /**
     * PatientAge 年龄
     */
    @Schema(description = "PatientAge 年龄")
    private String patientAge;

    /**
     * StudyDateAndTime 检查时间
     */
    @Schema(description = "StudyDateAndTime 检查时间")
    private Date studyDateAndTime;
}
