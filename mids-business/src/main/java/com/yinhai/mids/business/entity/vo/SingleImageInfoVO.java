package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 影像初始化数据
 *
 * @author zhuhs
 * @date 2024/8/27
 */
@Data
public class SingleImageInfoVO {
    /**
     * 计算序列ID
     */
    @Schema(description = "序列ID")
    private String seriesInstanceUid;


    /**
     * 切片厚度 SliceThickness
     */
    @Schema(description = "切片厚度 SliceThickness")
    private String sliceThickness;

    /**
     * KVP
     */
    @Schema(description = "KVP")
    private String kvp;

    /**
     * PixelSpacing
     */
    @Schema(description = "PixelSpacing 俩值逗号拼接")
    private String pixelSpacing;

    /**
     * InstitutionName
     */
    @Schema(description = "InstitutionName")
    private String institutionName;

    /**
     * Manufacturer
     */
    @Schema(description = "Manufacturer")
    private String manufacturer;

    /**
     * PatientName 患者姓名
     */
    @Schema(description = "PatientName 患者姓名")
    private String patientName;

    /**
     * 患者性别
     */
    @Schema(description = "患者性别")
    private String patientSex;

    /**
     * PatientAge 年龄
     */
    @Schema(description = "PatientAge 年龄")
    private String patientAge;

    /**
     * PatientID 患者ID
     */
    @Schema(description = "PatientID 患者ID")
    private String patientId;

    /**
     * StudyDateAndTime 检查时间
     */
    @Schema(description = "StudyDateAndTime 检查时间")
    private Date studyDateAndTime;
}
