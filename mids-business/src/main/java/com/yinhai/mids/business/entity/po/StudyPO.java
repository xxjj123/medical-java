package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 检查
 *
 * @author zhuhs
 * @date 2024/7/11 9:19
 */
@Schema(description = "检查")
@Data
@TableName(value = "tb_study")
public class StudyPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

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
     * 打印状态
     */
    @TableField(value = "print_status")
    @Schema(description = "打印状态")
    private String printStatus;

    /**
     * 推送状态
     */
    @TableField(value = "push_status")
    @Schema(description = "推送状态")
    private String pushStatus;

    /**
     * 操作医师
     */
    @TableField(value = "operate_doctor_user_id")
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

    /**
     * DICOM文件上传时间
     */
    @TableField(value = "upload_time")
    @Schema(description = "DICOM文件上传时间")
    private Date uploadTime;

    /**
     * 上传人员
     */
    @TableField(value = "upload_user_id")
    @Schema(description = "上传人员")
    private String uploadUserId;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "deleted")
    @Schema(description = "逻辑删除")
    private Boolean deleted;

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