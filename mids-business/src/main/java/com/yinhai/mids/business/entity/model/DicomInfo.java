package com.yinhai.mids.business.entity.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.File;
import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/7/5 15:13
 */
@Data
public class DicomInfo {

    /**
     * StudyInstanceUID
     */
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * SeriesInstanceUID
     */
    @Schema(description = "SeriesInstanceUID")
    private String seriesInstanceUid;

    /**
     * SOPInstanceUID
     */
    @Schema(description = "SOPInstanceUID")
    private String sopInstanceUid;

    /**
     * AccessionNumber 检查号
     */
    @Schema(description = "AccessionNumber 检查号")
    private String accessionNumber;

    /**
     * PatientID 患者ID
     */
    @Schema(description = "PatientID 患者ID")
    private String patientId;

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
     * StudyDateAndTime 检查时间
     */
    @Schema(description = "StudyDateAndTime 检查时间")
    private Date studyDateAndTime;

    /**
     * StudyDescription 检查描述
     */
    @Schema(description = "StudyDescription 检查描述")
    private String studyDescription;

    /**
     * SeriesNumber 序列号
     */
    @Schema(description = "SeriesNumber 序列号")
    private String seriesNumber;

    /**
     * SeriesDescription 序列描述
     */
    @Schema(description = "SeriesDescription 序列描述")
    private String seriesDescription;

    /**
     * InstanceNumber 实例顺序
     */
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

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
    @Schema(description = "PixelSpacing")
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
     * slicePosition
     */
    @Schema(description = "slicePosition")
    private double slicePosition;

    /**
     * DICOM文件
     */
    @Schema(description = "DICOM文件")
    private File file;
}
