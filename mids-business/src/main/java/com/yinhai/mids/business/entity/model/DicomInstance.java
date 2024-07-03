package com.yinhai.mids.business.entity.model;

import com.yinhai.mids.business.entity.po.InstancePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/7/2 11:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DicomInstance extends InstancePO {

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
     * SeriesDescription 序列描述
     */
    private String seriesDescription;

    private File instanceFile;
}
