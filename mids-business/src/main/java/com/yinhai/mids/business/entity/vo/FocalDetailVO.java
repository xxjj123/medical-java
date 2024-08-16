
package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FocalDetailVO {
    /**
     * SOPInstanceUID
     */
    @Schema(description = "SOPInstanceUID")
    private String sopInstanceUid;

    /**
     * 病灶位置
     */
    @Schema(description = "词条信息")
    private String vocabularyEntry;

    /**
     * 病灶类型
     */
    @Schema(description = "结节类型")
    private String type;

    /**
     * 结节索引
     */
    @Schema(description = "结节索引")
    private Integer boxIndex;

    /**
     * 叶段编号
     */
    @Schema(description = "肺段")
    private Integer lobeSegment;

    /**
     * 叶片
     */
    @Schema(description = "肺叶")
    private String lobe;

    /**
     * 体积
     */
    @Schema(description = "体积")
    private Double volume;

    /**
     * 风险代码
     */
    @Schema(description = "风险代码")
    private Integer riskCode;


    /**
     * CT测量值
     */
    @Schema(description = "CT测量值")
    private CtMeasures ctMeasures;

    /**
     * 椭圆轴
     */
    @Schema(description = "病灶椭圆轴")
    private EllipsoidAxis ellipsoidAxis;

    /**
     * 边界框
     */
    @Schema(description = "边界框")
    private Integer[] bbox;

    @Data
    public static class CtMeasures {
        /**
         * 平均值
         */
        @Schema(description = "平均值")
        private Double mean;

        /**
         * 最小值
         */
        @Schema(description = "最小值")
        private Double minimum;

        /**
         * 最大值
         */
        @Schema(description = "最大值")
        private Double maximum;
    }

    @Data
    public static class EllipsoidAxis {
        /**
         * 最小轴
         */
        @Schema(description = "短轴⻓度")
        private Double least;

        /**
         * 次小轴
         */
        @Schema(description = "最⼩轴⻓度")
        private Double minor;

        /**
         * 主轴
         */
        @Schema(description = "⻓轴⻓度")
        private Double major;
    }
}
