package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class SeriesVO {

    /**
     * 序列ID
     */
    @Schema(description = "序列ID")
    private String seriesId;

    /**
     * 计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常
     */
    @Schema(description = "计算状态 1-等待计算 2-计算中 3-计算成功 4-计算失败 5-计算取消 6-计算异常")
    private String mprStatus;
    /**
     * 图像数量
     */
    @Schema(description = "图像数量")
    private Integer imageCount;

    /**
     * 图像数量
     */
    @Schema(description = "横切面数量")
    private Integer axialCount;

    /**
     * 图像数量
     */
    @Schema(description = "冠状面数量")
    private Integer coronalCount;

    /**
     * 图像数量
     */
    @Schema(description = "矢状面数量")
    private Integer sagittalCount;
}




