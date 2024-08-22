package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/8/21
 */
@Data
@Schema(description = "结节信息")
public class NoduleVO {

    /**
     * 计算序列ID
     */
    @Schema(description = "计算序列ID")
    private String computeSeriesId;

    /**
     * 检查ID
     */
    @Schema(description = "检查ID")
    private String studyId;

    /**
     * 序列ID
     */
    @Schema(description = "序列ID")
    private String seriesId;

    /**
     * StudyInstanceUID
     */
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * SeriesInstanceUID
     */
    @Schema(description = "SeriesInstanceUID")
    private String seriesInstanceUid;

    /**
     * 阴阳性
     */
    @Schema(description = "阴阳性")
    private Boolean hasLesion;

    /**
     * 图像数量
     */
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 结节病变列表
     */
    @Schema(description = "结节病变列表")
    private List<NoduleLesionVO> noduleLesionList;
}
