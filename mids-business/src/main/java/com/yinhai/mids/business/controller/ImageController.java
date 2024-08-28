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
import javax.validation.constraints.NotBlank;

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
}
