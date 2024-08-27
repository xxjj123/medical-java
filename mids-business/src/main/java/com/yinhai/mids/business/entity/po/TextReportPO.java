package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 文本报告
 *
 * @author zhuhs
 * @date 2024/8/22
 */
@Schema(description = "文本报告")
@Data
@TableName(value = "tb_text_report")
public class TextReportPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_compute_series.id
     */
    @TableField(value = "compute_series_id")
    @Schema(description = "tb_compute_series.id")
    private String computeSeriesId;

    /**
     * 报告类型 calcium, pneumonia, nodule, frac
     */
    @TableField(value = "report_type")
    @Schema(description = "报告类型 calcium, pneumonia, nodule, frac")
    private String reportType;

    /**
     * 患者编号
     */
    @TableField(value = "patient_id", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "患者编号")
    private String patientId;

    /**
     * 检查号
     */
    @TableField(value = "accession_number", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "检查号")
    private String accessionNumber;

    /**
     * 检查日期
     */
    @TableField(value = "study_date", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "检查日期")
    private Date studyDate;

    /**
     * 患者姓名
     */
    @TableField(value = "patient_name", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "患者姓名")
    private String patientName;

    /**
     * 患者性别
     */
    @TableField(value = "patient_sex", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "患者性别")
    private String patientSex;

    /**
     * 影像所见
     */
    @TableField(value = "finding", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "影像所见")
    private String finding;

    /**
     * 影像诊断
     */
    @TableField(value = "diagnosis", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "影像诊断")
    private String diagnosis;

    /**
     * 报告日期
     */
    @TableField(value = "report_date", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "报告日期")
    private Date reportDate;

    /**
     * 报告医生
     */
    @TableField(value = "report_doctor", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "报告医生")
    private String reportDoctor;

    /**
     * 审核医生
     */
    @TableField(value = "audit_doctor", updateStrategy = FieldStrategy.ALWAYS)
    @Schema(description = "审核医生")
    private String auditDoctor;

    /**
     * 数据创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "数据创建时间")
    private Date createTime;

    /**
     * 数据修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "数据修改时间")
    private Date updateTime;
}