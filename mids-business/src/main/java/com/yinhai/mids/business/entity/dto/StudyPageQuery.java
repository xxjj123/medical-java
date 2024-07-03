package com.yinhai.mids.business.entity.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/7/2 15:56
 */
@Data
public class StudyPageQuery {

    /**
     * 开始日期
     */
    @Parameter(description = "开始日期")
    private Date startDate;

    /**
     * 结束日期
     */
    @Parameter(description = "结束日期")
    private Date endDate;

    /**
     * AccessionNumber 检查号
     */
    @Parameter(description = "AccessionNumber 检查号")
    private String accessionNumber;

    /**
     * PatientID 患者ID
     */
    @Parameter(description = "PatientID 患者ID")
    private String patientId;

    /**
     * PatientName 患者姓名
     */
    @Parameter(description = "PatientName 患者姓名")
    private String patientName;

    /**
     * 算法类型
     */
    @Parameter(description = "算法类型")
    private String algorithmType;

    /**
     * 计算状态
     */
    @Parameter(description = "计算状态")
    private String computeStatus;

    /**
     * 我的收藏
     */
    @Parameter(description = "我的收藏")
    private Boolean myFavorite;

}
