package com.yinhai.mids.business.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 报告公共字段
 *
 * @author zhuhs
 * @date 2024/8/29
 */
@Data
public class ReportCommon {
    /**
     * 计算序列ID
     */
    @NotBlank(message = "计算序列ID不能为空")
    @Schema(description = "计算序列ID")
    private String computeSeriesId;
    /**
     * 患者编号
     */
    @Schema(description = "患者编号")
    private String patientId;
    /**
     * 检查号
     */
    @Schema(description = "检查号")
    private String accessionNumber;
    /**
     * 检查日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检查日期")
    private Date studyDate;
    /**
     * 患者姓名
     */
    @Schema(description = "患者姓名")
    private String patientName;
    /**
     * 患者性别
     */
    @Schema(description = "患者性别")
    private String patientSex;
    /**
     * 患者年龄
     */
    @Schema(description = "患者年龄")
    private String patientAge;
    /**
     * 检查项目
     */
    @Schema(description = "检查项目")
    private String examinedName;
    /**
     * 影像所见
     */
    @Schema(description = "影像所见")
    private String finding;
    /**
     * 影像诊断
     */
    @Schema(description = "影像诊断")
    private String diagnosis;
    /**
     * 报告日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报告日期")
    private Date reportDate;
    /**
     * 报告医生
     */
    @Schema(description = "报告医生")
    private String reportDoctor;
    /**
     * 审核医生
     */
    @Schema(description = "审核医生")
    private String auditDoctor;
}
