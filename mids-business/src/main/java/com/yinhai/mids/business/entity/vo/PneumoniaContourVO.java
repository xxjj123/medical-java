package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 肺炎轮廓
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Data
@Schema(description = "肺炎轮廓")
public class PneumoniaContourVO {
    /**
     * tb_compute_series.id
     */
    @Schema(description = "tb_compute_series.id")
    private String computeSeriesId;

    /**
     * view name
     */
    @Schema(description = "view name")
    private String viewName;

    /**
     * InstanceNumber 实例顺序
     */
    @Schema(description = "InstanceNumber 实例顺序")
    private Integer instanceNumber;

    /**
     * 轮廓点位
     */
    @Schema(description = "轮廓点位")
    private String points;
}
