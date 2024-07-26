package com.yinhai.mids.business.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * vti文件
 *
 * @author zhuhs
 * @date 2024/7/18 11:48
 */
@Schema(description = "vti文件")
@Data
@TableName(value = "tb_vti")
public class VtiPO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**
     * tb_study.id
     */
    @TableField(value = "study_id")
    @Schema(description = "tb_study.id")
    private String studyId;

    /**
     * tb_series.id
     */
    @TableField(value = "series_id")
    @Schema(description = "tb_series.id")
    private String seriesId;

    /**
     * StudyInstanceUID
     */
    @TableField(value = "study_instance_uid")
    @Schema(description = "StudyInstanceUID")
    private String studyInstanceUid;

    /**
     * SeriesInstanceUID
     */
    @TableField(value = "series_instance_uid")
    @Schema(description = "SeriesInstanceUID")
    private String seriesInstanceUid;

    /**
     * view name
     */
    @TableField(value = "view_name")
    @Schema(description = "view name")
    private String viewName;

    /**
     * view index
     */
    @TableField(value = "view_index")
    @Schema(description = "view index")
    private Integer viewIndex;

    /**
     * 访问路径
     */
    @TableField(value = "access_path")
    @Schema(description = "访问路径")
    private String accessPath;

    /**
     * 数据创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "数据创建时间")
    private Date createTime;

    /**
     * 数据修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "数据修改时间")
    private Date updateTime;
}