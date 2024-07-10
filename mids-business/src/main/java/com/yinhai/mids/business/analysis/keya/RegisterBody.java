package com.yinhai.mids.business.analysis.keya;

import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/6/30 22:16
 */
@Data
public class RegisterBody {

    /**
     * 申请编号 必须
     */
    String applyId;
    /**
     * 检查编号 必须
     */
    String accessionNumber;
    /**
     * StudyInstanceUID 必须
     */
    String studyInstanceUID;
    /**
     * 医院ID(C-Store传输时当AET使⽤) 必须
     */
    String hospitalId;
    /**
     * 标准检查项⽬名称 （多个使⽤ ; 分割） 必须
     */
    String examinedName;
    /**
     * 患者姓名
     */
    String patientName;
    /**
     * 性别
     */
    String patientSex;
    /**
     * 年龄
     */
    String patientAge;
    /**
     * 设备
     */
    String modality;
    /**
     * 检查日期
     */
    String studyDate;
    /**
     * 检查部位
     */
    String bodyPartExamined;
    /**
     * 检查描述
     */
    String description;
    /**
     * AI分析完成回调地址
     */
    String callbackUr;

}
