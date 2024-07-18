package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/2 15:55
 */
@Data
public class StudyPageVO {

    /**
     * 检查研究主键
     */
    @Schema(description = "检查ID")
    private String studyId;

    /**
     * StudyInstanceUID 研究实例UID
     */
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

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
     * 序列数
     */
    @Schema(description = "序列数")
    private Integer seriesCount;

    /**
     * 我的收藏
     */
    @Schema(description = "我的收藏")
    private Boolean myFavorite;

    /**
     * 算法类型
     */
    @Schema(description = "算法类型")
    private String algorithmType;

    /**
     * 打印状态
     */
    @Schema(description = "打印状态")
    private String printStatus;

    /**
     * 推送状态
     */
    @Schema(description = "推送状态")
    private String pushStatus;

    /**
     * 操作医师
     */
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

    List<ComputeSeriesPageVO> seriesList;
}
