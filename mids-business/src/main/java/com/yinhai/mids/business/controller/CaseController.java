package com.yinhai.mids.business.controller;

import cn.hutool.core.date.DateUtil;
import com.yinhai.mids.business.service.CaseService;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.RestServiceKit;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/10/14
 */
@Validated
@Tag(name = "案例")
@RestService("case")
public class CaseController {

    @Resource
    private CaseService caseService;

    @Operation(summary = "案例文件上传")
    @PostMapping("uploadCaseFile")
    public void uploadCaseFile(@RequestPart("caseFile") @NotNull(message = "上传文件不能为空") MultipartFile caseFile) throws IOException {
        caseService.createCase(caseFile);
        RestServiceKit.setData("uploadTime", DateUtil.formatDateTime(DbClock.now()));
    }
}
