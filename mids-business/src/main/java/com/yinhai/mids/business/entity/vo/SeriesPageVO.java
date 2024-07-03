package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/7/2 16:09
 */
@Data
public class SeriesPageVO {

    /**
     * 序列主键
     */
    @Schema(description = "主键")
    private String seriesId;

    /**
     * 检查研究主键
     */
    @Schema(description = "检查研究主键")
    private String studyId;

    /**
     * StudyInstanceUID 研究实例UID
     */
    @Schema(description = "StudyInstanceUID 研究实例UID")
    private String studyUid;

    /**
     * SeriesInstanceUID 序列实例UID
     */
    @Schema(description = "SeriesInstanceUID 序列实例UID")
    private String seriesUid;

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
    private String algorithmType;

    /**
     * 计算状态
     */
    @Schema(description = "计算状态")
    private String computeStatus;

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

}
