package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 影像初始化数据
 *
 * @author zhuhs
 * @date 2024/8/27
 */
@Data
public class ImageInitInfoVO {
    /**
     * 计算序列ID
     */
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

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
     * 图像数量
     */
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 横切面数量
     */
    @Schema(description = "横切面数量")
    private Integer axialCount;

    /**
     * 冠状面数量
     */
    @Schema(description = "冠状面数量")
    private Integer coronalCount;

    /**
     * 矢状面数量
     */
    @Schema(description = "矢状面数量")
    private Integer sagittalCount;

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

    /**
     * Instance metadata
     */
    private List<InstanceMetadata> instanceMetadataList;
}
