package com.yinhai.mids.business.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 肺炎病变
 *
 * @author zhuhs
 * @date 2024/10/28
 */
@Data
@Schema(description = "肺炎病变")
public class PneumoniaLesionVO {
    /**
     * 病变ID
     */
    @Schema(description = "病变ID")
    private String id;
    /**
     * tb_compute_series.id
     */
    @Schema(description = "tb_compute_series.id")
    private String computeSeriesId;
    /**
     * 选中状态
     */
    @Schema(description = "选中状态")
    private Boolean checked;
    /**
     * 肺叶名称
     */
    @Schema(description = "肺叶名称")
    private String lobeName;
    /**
     * 肺叶体积
     */
    @Schema(description = "肺叶体积")
    private Double lobeVolume;
    /**
     * 病变体积
     */
    @Schema(description = "病变体积")
    private Double diseaseVolume;
    /**
     * 病变平均HU值
     */
    @Schema(description = "病变平均HU值")
    private Double intensity;
    /**
     * 病变类型 GGO/HD
     */
    @Schema(description = "病变类型 GGO/HD")
    private String diseaseClass;
}
