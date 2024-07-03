package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 序列表
 *
 * @author zhuhs
 * @date 2024/7/2 9:40
 */
@Schema(description = "序列表")
@Data
@TableName(value = "tb_series")
public class SeriesPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_study.id
     */
    @TableField(value = "study_id")
    @Schema(description = "tb_study.id")
    private String studyId;

    /**
     * StudyInstanceUID 研究实例UID
     */
    @TableField(value = "study_uid")
    @Schema(description = "StudyInstanceUID 研究实例UID")
    private String studyUid;

    /**
     * SeriesInstanceUID 序列实例UID
     */
    @TableField(value = "series_uid")
    @Schema(description = "SeriesInstanceUID 序列实例UID")
    private String seriesUid;

    /**
     * SeriesNumber 序列号
     */
    @TableField(value = "series_number")
    @Schema(description = "SeriesNumber 序列号")
    private String seriesNumber;

    /**
     * SeriesDescription 序列描述
     */
    @TableField(value = "series_description")
    @Schema(description = "SeriesDescription 序列描述")
    private String seriesDescription;

    /**
     * 图像数量
     */
    @TableField(value = "image_count")
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 算法类型
     */
    @TableField(value = "algorithm_type")
    @Schema(description = "算法类型")
    private String algorithmType;

    /**
     * 计算状态
     */
    @TableField(value = "compute_status")
    @Schema(description = "计算状态")
    private String computeStatus;

    /**
     * 操作状态
     */
    @TableField(value = "operate_status")
    @Schema(description = "操作状态")
    private String operateStatus;

    /**
     * 操作医师
     */
    @TableField(value = "operate_doctor_user_id")
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

    /**
     * 逻辑删除
     */
    @TableField(value = "deleted")
    @Schema(description = "逻辑删除")
    private Integer deleted;

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