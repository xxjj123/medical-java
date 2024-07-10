package com.yinhai.mids.business.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/7/9 16:34
 */
@Data
public class RegisterDicomInfo {

    /**
     * StudyInstanceUID 研究实例UID
     */
    private String studyUid;

    /**
     * AccessionNumber 检查号
     */
    private String accessionNumber;

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

}
