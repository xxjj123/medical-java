package com.yinhai.mids.business.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/12 14:13
 */
@Validated
@Tag(name = "分析回调")
@RestService("callback")
public class CallbackController {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeSeriesService computeSeriesService;

    @Operation(summary = "AI结果推送")
    @PostMapping("computePush")
    public void computePush(@RequestParam Map<String, Object> pushParamMap) {
        computeSeriesService.onComputePush(pushParamMap);
    }

    @Operation(summary = "3D解析结果推送")
    @PostMapping("analysePush")
    public void analysePush(@RequestPart @NotNull(message = "回传文件不能为空") MultipartFile file,
                            @RequestParam @NotBlank(message = "申请ID不能为空") String applyId) {

    }

}
