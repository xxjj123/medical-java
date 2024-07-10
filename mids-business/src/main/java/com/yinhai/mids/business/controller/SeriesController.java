package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.service.SeriesService;
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
 * @date 2024/7/10 14:38
 */
@Validated
@Tag(name = "序列")
@RestService("study")
public class SeriesController {

    @Resource
    private SeriesService seriesService;

    @Operation(summary = "重新分析")
    @PostMapping("reAnalyse")
    public void reAnalyse(@RequestParam @NotBlank(message = "序列ID不能为空") String seriesId) {
        seriesService.reAnalyse(seriesId);
    }

}
