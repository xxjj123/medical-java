package com.yinhai.mids.business.entity.vo;

import com.yinhai.mids.common.module.validate.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 结节病变
 *
 * @author zhuhs
 * @date 2024/8/20
 */
@Data
@Schema(description = "结节病变")
public class NoduleLesionVO {
    /**
     * 病变ID
     */
    @NotBlank(message = "病变ID不能为空")
    @Schema(description = "病变ID")
    private String id;
    /**
     * 计算序列ID tb_compute_series.id
     */
    @NotBlank(message = "计算序列ID不能为空")
    @Schema(description = "计算序列ID")
    private String computeSeriesId;
    /**
     * SOPInstanceUID
     */
    @Schema(description = "SOPInstanceUID")
    private String sopInstanceUid;
    /**
     * IM
     */
    @Schema(description = "IM")
    private Integer im;
    /**
     * 结节类型
     */
    @NotBlank(message = "结节类型不能为空")
    @AllowedValues(dictType = "NODULE_TYPE", delimited = true, message = "结节类型不正确")
    @Schema(description = "结节类型 【字典码值】")
    private String type;
    /**
     * 肺段
     */
    @NotNull(message = "肺段不能为空")
    @AllowedValues(dictType = "LOBE_SEGMENT", delimited = true, message = "肺段不正确")
    @Schema(description = "肺段 【字典码值】")
    private Integer lobeSegment;
    /**
     * 肺叶
     */
    @Schema(description = "肺叶")
    private String lobe;
    /**
     * 体积
     */
    @NotNull(message = "体积不能为空")
    @Schema(description = "体积")
    private Double volume;
    /**
     * 风险
     */
    @AllowedValues(dictType = "NODULE_RISK", delimited = true, message = "风险不正确")
    @Schema(description = "风险 【字典码值】")
    private Integer riskCode;
    /**
     * CT措施 平均HU
     */
    @NotNull(message = "平均HU不能为空")
    @Schema(description = "CT措施 平均HU")
    private Double ctMeasuresMean;
    /**
     * 病灶椭圆轴 ⻓轴⻓度
     */
    @NotNull(message = "长径不能为空")
    @Schema(description = "长径（病灶椭圆轴 ⻓轴⻓度）")
    private Double ellipsoidAxisMajor;
    /**
     * 病灶椭圆轴 短轴⻓度
     */
    @NotNull(message = "短径不能为空")
    @Schema(description = "短径（病灶椭圆轴 短轴⻓度）")
    private Double ellipsoidAxisLeast;
    /**
     * 坐标点
     */
    @Schema(description = "坐标点 x,x,y,y,z,z")
    private String points;
    /**
     * 肺段顺序
     */
    @Schema(description = "肺段顺序")
    private Integer lobeSegmentSort;
    /**
     * 结节类型顺序
     */
    @Schema(description = "结节类型顺序")
    private Integer typeSort;
}
