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
public class FracLesionVO {
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
     * 骨折所在的边：左侧、右侧
     */
    @Schema(description = "骨折所在的边：左侧、右侧")
    private String ribSide;
    /**
     * 骨折位置：front(前段)/middle(腋段)/back(后段)
     */
    @Schema(description = "骨折位置：front(前段)/middle(腋段)/back(后段)")
    private String ribType;
    /**
     * 骨折所在侧的肋骨编号
     */
    @Schema(description = "骨折所在侧的肋骨编号")
    private Integer ribNum;
    /**
     * 骨折类型：old(陈旧性骨折)/buckle(骨皮质扭曲)/displaced(错位性骨折)/nodisplaced(非错位性骨折)
     */
    @Schema(description = "骨折类型：old(陈旧性骨折)/buckle(骨皮质扭曲)/displaced(错位性骨折)/nodisplaced(非错位性骨折)")
    private String fracClass;
    /**
     * 骨折区域的bbox
     */
    @Schema(description = "骨折区域的bbox")
    private String fracBBox;
}
