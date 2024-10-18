package com.yinhai.mids.business.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/7/2 16:09
 */
@Data
public class CaseSeriesVO {

    /**
     * 计算任务ID
     */
    @Schema(description = "计算任务ID")
    private String computeTaskId;

    /**
     * 序列信息ID
     */
    @TableId(value = "series_info_id", type = IdType.ASSIGN_ID)
    @Schema(description = "序列信息ID")
    private String seriesInfoId;


    /**
     * 检查信息ID
     */
    @TableField(value = "study_info_id")
    @Schema(description = "检查信息ID")
    private String studyInfoId;

    /**
     * SeriesNumber 序列号
     */
    @Schema(description = "SeriesNumber 序列号")
    private String seriesNumber;

    /**
     * SeriesDescription 序列描述
     */
    @Schema(description = "SeriesDescription 序列描述")
    private String seriesDescription;

    /**
     * 图像数量
     */
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 算法类型
     */
    @Schema(description = "算法类型")
    private Integer computeType;

    /**
     * 计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常
     */
    @TableField(value = "compute_status")
    @Schema(description = "计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常")
    private Integer computeStatus;

    /**
     * 操作状态
     */
    @Schema(description = "操作状态")
    private String operateStatus;

    /**
     * 操作医师
     */
    @Schema(description = "操作医师")
    private String operateDoctorUserId;

    /**
     * 我的收藏
     */
    @Schema(description = "我的收藏")
    private Boolean myFavorite;
}
