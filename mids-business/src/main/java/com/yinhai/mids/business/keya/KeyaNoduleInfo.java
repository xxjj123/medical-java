package com.yinhai.mids.business.keya;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/28
 */
@Data
public class KeyaNoduleInfo {
    private String vocabularyEntry;
    private String type;
    private Integer lobeSegment;
    private String lobe;
    private Double volume;
    private CtMeasures ctMeasures;
    private EllipsoidAxis ellipsoidAxis;
    private Integer riskCode;

    private List<Point> points;
    private List<Annotation> annotation;

    @JsonProperty("SOPInstanceUID")
    private String sopInstanceUID;

    private String description;

    @JsonProperty("sOPInstanceUID")
    private String instanceUID;

    @Data
    public static class CtMeasures {
        private double mean;
        private double minimum;
        private double maximum;
    }

    @Data
    public static class EllipsoidAxis {
        private double least;
        private double minor;
        private double major;
    }

    @Data
    public static class Point {
        private int x;
        private int y;
        private int z;
    }

    @Data
    public static class Annotation {
        private String type;
        private List<Point> points;
    }
}
