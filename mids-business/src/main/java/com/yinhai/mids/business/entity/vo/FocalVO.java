package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FocalVO {
    /**
     * SeriesInstanceUID 序列实例UID
     */
    @Schema(description = "SOPInstanceUID 服务对象对实例UID")
    private String seriesUid;

    /**
     * 诊断结果
     */
    @Schema(description = "诊断结果")
    private String diagnosis;

    /**
     * 诊断发现
     */
    @Schema(description = "诊断发现")
    private String finding;

    /**
     * 阴阳性
     */
    @Schema(description = "阴阳性")
    private Boolean hasLesion;

    /**
     * 病灶数量
     */
    @Schema(description = "病灶数量")
    private Integer number;

    List<FocalDetailVO> focalDetailList;

}
