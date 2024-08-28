package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 序列
 *
 * @author zhuhs
 * @date 2024/7/11 9:21
 */
@Accessors(chain = true)
@Schema(description = "序列")
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
     * StudyInstanceUID
     */
    @TableField(value = "study_instance_uid")
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * SeriesInstanceUID
     */
    @TableField(value = "series_instance_uid")
    @Schema(description = "SeriesInstanceUID")
    private String seriesInstanceUid;

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
     * 计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常
     */
    @TableField(value = "mpr_status")
    @Schema(description = "计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常")
    private String mprStatus;

    /**
     * 发起计算时间
     */
    @TableField(value = "mpr_start_time")
    @Schema(description = "发起mpr重建时间")
    private Date mprStartTime;

    /**
     * 计算返回结果
     */
    @TableField(value = "mpr_response")
    @Schema(description = "mpr重建返回结果")
    private String mprResponse;

    /**
     * 异常信息
     */
    @TableField(value = "mpr_error_message")
    @Schema(description = "mpr异常信息")
    private String mprErrorMessage;

    /**
     * 图像数量
     */
    @TableField(value = "image_count")
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 横切面数量
     */
    @TableField(value = "axial_count")
    @Schema(description = "横切面数量")
    private Integer axialCount;

    /**
     * 冠状面数量
     */
    @TableField(value = "coronal_count")
    @Schema(description = "冠状面数量")
    private Integer coronalCount;

    /**
     * 矢状面数量
     */
    @TableField(value = "sagittal_count")
    @Schema(description = "矢状面数量")
    private Integer sagittalCount;

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