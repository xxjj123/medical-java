package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 检查信息
 *
 * @author zhuhs
 * @date 2024/10/14
 */
@Schema(description = "检查")
@Data
@TableName(value = "tb_study_info")
public class StudyInfoPO {
    /**
     * 检查ID
     */
    @TableId(value = "study_id", type = IdType.ASSIGN_ID)
    @Schema(description = "检查ID")
    private String studyId;

    /**
     * 案例ID
     */
    @TableField(value = "case_id")
    @Schema(description = "案例ID")
    private String caseId;

    /**
     * StudyInstanceUID
     */
    @TableField(value = "study_instance_uid")
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * AccessionNumber 检查号
     */
    @TableField(value = "accession_number")
    @Schema(description = "AccessionNumber 检查号")
    private String accessionNumber;

    /**
     * PatientID 患者ID
     */
    @TableField(value = "patient_id")
    @Schema(description = "PatientID 患者ID")
    private String patientId;

    /**
     * PatientName 患者姓名
     */
    @TableField(value = "patient_name")
    @Schema(description = "PatientName 患者姓名")
    private String patientName;

    /**
     * PatientSex 患者性别
     */
    @TableField(value = "patient_sex")
    @Schema(description = "PatientSex 患者性别")
    private String patientSex;

    /**
     * PatientAge 年龄
     */
    @TableField(value = "patient_age")
    @Schema(description = "PatientAge 年龄")
    private String patientAge;

    /**
     * StudyDateAndTime 检查时间
     */
    @TableField(value = "study_date_and_time")
    @Schema(description = "StudyDateAndTime 检查时间")
    private Date studyDateAndTime;

    /**
     * StudyDescription 检查描述
     */
    @TableField(value = "study_description")
    @Schema(description = "StudyDescription 检查描述")
    private String studyDescription;

    /**
     * 切片厚度 SliceThickness
     */
    @TableField(value = "slice_thickness")
    @Schema(description = "切片厚度 SliceThickness")
    private String sliceThickness;

    /**
     * KVP
     */
    @TableField(value = "kvp")
    @Schema(description = "KVP")
    private String kvp;

    /**
     * PixelSpacing
     */
    @TableField(value = "pixel_spacing")
    @Schema(description = "PixelSpacing")
    private String pixelSpacing;

    /**
     * InstitutionName
     */
    @TableField(value = "institution_name")
    @Schema(description = "InstitutionName")
    private String institutionName;

    /**
     * Manufacturer
     */
    @TableField(value = "manufacturer")
    @Schema(description = "Manufacturer")
    private String manufacturer;

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