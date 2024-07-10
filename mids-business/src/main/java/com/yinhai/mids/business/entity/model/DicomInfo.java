package com.yinhai.mids.business.entity.model;

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
     * StudyInstanceUID 研究实例UID
     */
    private String studyUid;

    /**
     * SeriesInstanceUID 序列实例UID
     */
    private String seriesUid;

    /**
     * SOPInstanceUID 服务对象对实例UID
     */
    private String instanceUid;

    /**
     * InstanceNumber 实例顺序
     */
    private Integer instanceNumber;

    /**
     * AccessionNumber 检查号
     */
    private String accessionNumber;

    /**
     * PatientID 患者ID
     */
    private String patientId;

    /**
     * PatientName 患者姓名
     */
    private String patientName;

    /**
     * PatientAge 年龄
     */
    private String patientAge;

    /**
     * StudyDateAndTime 检查时间
     */
    private Date studyDatetime;

    /**
     * StudyDescription 检查描述
     */
    private String studyDescription;

    /**
     * SeriesNumber 序列号
     */
    private String seriesNumber;

    /**
     * SeriesDescription 序列描述
     */
    private String seriesDescription;

    /**
     * DICOM文件
     */
    private File file;

}
