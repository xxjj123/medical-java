package com.yinhai.mids.business.analysis.keya;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/9 17:48
 */
@Data
public class KeyaAnalyseResult {
    private Result result;
    private String aiResultUrl;

    @Data
    public static class Result {
        private Calcium calcium;
        private Pneumonia pneumonia;
        private Nodule nodule;
        private Frac frac;

        @Data
        public static class Calcium {
            private String finding;
            private boolean hasLesion;
            private int number;
            @JsonProperty("SeriesInstanceUID")
            private String seriesInstanceUID;
        }

        @Data
        public static class Pneumonia {
            private String diagnosis;
            private String finding;
            private boolean hasLesion;
            private int number;
            @JsonProperty("SeriesInstanceUID")
            private String seriesInstanceUID;
        }

        @Data
        public static class Nodule {
            private String diagnosis;
            private String finding;
            private boolean hasLesion;
            private int number;
            private List<VolumeDetail> volumeDetailList;
            @JsonProperty("SeriesInstanceUID")
            private String seriesInstanceUID;

            @Data
            public static class VolumeDetail {
                private List<Annotation> annotation;
                private String vocabularyEntry;
                private String type;
                private int lobeSegment;
                private String lobe;
                private double volume;
                private CtMeasures ctMeasures;
                private EllipsoidAxis ellipsoidAxis;
                private int riskCode;
                @JsonProperty("SOPInstanceUID")
                private String sopInstanceUID;

                @Data
                public static class Annotation {
                    private String type;
                    private List<Point> points;

                    @Data
                    public static class Point {
                        private int x;
                        private int y;
                        int z;
                    }
                }

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
            }
        }

        @Data
        public static class Frac {
            private String diagnosis;
            private String finding;
            private boolean hasLesion;
            private int number;
            @JsonProperty("SeriesInstanceUID")
            private String seriesInstanceUID;
        }
    }
}
