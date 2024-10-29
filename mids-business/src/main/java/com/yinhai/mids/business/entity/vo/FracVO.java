package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 骨折信息
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Data
@Schema(description = "骨折信息")
public class FracVO {

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
     * 肺炎病变列表
     */
    @Schema(description = "骨折病变列表")
    private List<FracLesionVO> fracLesionList;
}
