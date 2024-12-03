package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.vo.SpineInfoVO;
import com.yinhai.mids.business.service.SpineService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/12/2
 */
@Validated
@Tag(name = "脊柱")
@RestService("spine")
public class SpineController {

    @Resource
    private SpineService spineService;

    @Operation(summary = "获取脊柱检测信息")
    @PostMapping("getSpineInfo")
    public SpineInfoVO querySpineInfo(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) throws IOException {
        return spineService.querySpineInfo(computeSeriesId);
    }

}
