package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.vo.ImageInitInfoVO;
import com.yinhai.mids.business.service.ImageService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
@Validated
@Tag(name = "影像相关")
@RestService("image")
public class ImageController {

    @Resource
    private ImageService imageService;

    @Operation(summary = "查询影像初始化数据")
    @PostMapping("initInfo")
    public ImageInitInfoVO queryStudyInfo(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return imageService.queryInitInfo(computeSeriesId);
    }

    @Operation(summary = "下载影像切片")
    @PostMapping("downloadSlice")
    public void downloadSlice(@RequestParam @NotBlank(message = "序列ID不能为空") String seriesId,
                              @RequestParam @NotBlank(message = "切片方向不能为空") String viewName,
                              @RequestParam @NotNull(message = "切片序列不能为空") Integer viewIndex,
                              HttpServletResponse response) {
        imageService.downloadSlice(seriesId, viewName, viewIndex, response);
    }

    @Operation(summary = "下载3D模型")
    @PostMapping("download3dModel")
    public void download3dModel(@RequestParam @NotBlank(message = "序列ID不能为空") String seriesId,
                                HttpServletResponse response) {
        imageService.download3dModel(seriesId, response);
    }

    @Operation(summary = "下载影像DICOM")
    @PostMapping("downloadDicom")
    public void downloadDicom(@RequestParam @NotBlank(message = "实例ID不能为空") String instanceId,
                              HttpServletResponse response) {
        imageService.downloadDicom(instanceId, response);
    }
}
