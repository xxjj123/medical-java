package com.yinhai.mids.business.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.dto.MprPushParam;
import com.yinhai.mids.business.service.KeyaService;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/10/18
 */
@Validated
@Tag(name = "开放接口")
@RestService("open")
public class OpenController {

    private static final Log log = LogFactory.get();

    @Resource
    private KeyaService keyaService;

    @Resource
    private MprService mprService;

    @Operation(summary = "科亚AI分析申请推送")
    @PostMapping("keyaApplyPush")
    public void keyaApplyPush(@RequestParam Map<String, Object> pushParamMap) {
        keyaService.onApplyPush(pushParamMap);
    }

    @Operation(summary = "MPR解析结果推送")
    @PostMapping("mprPush")
    public void mprPush(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = false) @NotNull(message = "type不能为空") String type,
            @RequestParam(value = "seriesId") @NotNull(message = "序列ID不能为空") String seriesId,
            @RequestParam(value = "applyId") @NotNull(message = "申请编号不能为空") String applyId,
            @RequestParam(value = "code") @NotNull(message = "code不能为空") String code,
            @RequestParam(value = "message") String message) {
        MprPushParam mprPushParam = new MprPushParam();
        mprPushParam.setType(type);
        mprPushParam.setSeriesId(seriesId);
        mprPushParam.setApplyId(applyId);
        mprPushParam.setCode(code);
        mprPushParam.setMessage(message);
        mprPushParam.setPushTime(DbClock.now());

        if (StrUtil.equals("1", code) && file != null) {
            try {
                File tempZip = Files.createTempFile(null, ".zip").toFile();
                FileUtil.writeFromStream(file.getInputStream(), tempZip);
                mprPushParam.setFile(tempZip);
            } catch (IOException e) {
                log.error(e);
                mprPushParam.setException(e);
            }
        }
        mprService.onMprPush(mprPushParam);
    }
}