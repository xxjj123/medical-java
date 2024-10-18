package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.service.KeyaService;
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
 * @date 2024/10/18
 */
@Validated
@Tag(name = "开放接口")
@RestService("open")
public class OpenController {

    @Resource
    private KeyaService keyaService;

    @Operation(summary = "科亚AI分析申请推送")
    @PostMapping("keyaApplyPush")
    public void computePush(@RequestParam Map<String, Object> pushParamMap) {
        keyaService.onApplyPush(pushParamMap);
    }
}
