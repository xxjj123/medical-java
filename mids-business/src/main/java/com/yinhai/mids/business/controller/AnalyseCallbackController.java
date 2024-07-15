package com.yinhai.mids.business.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.service.SeriesComputeService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/12 14:13
 */
@Validated
@Tag(name = "分析回调")
@RestService("callback")
public class AnalyseCallbackController {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesComputeService seriesComputeService;

    @Operation(summary = "AI结果推送")
    @PostMapping("push")
    public void push(@RequestParam Map<String, Object> pushParamMap) {
        seriesComputeService.onAnalysePush(pushParamMap);
    }

}
