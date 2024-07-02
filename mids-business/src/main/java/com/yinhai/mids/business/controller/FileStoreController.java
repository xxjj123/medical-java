package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/1 16:55
 */
@Tag(name = "文件存储服务")
@RestService("fs")
public class FileStoreController {

    @Resource
    private FileStoreService fileStoreService;

    @Operation(summary = "文件上传")
    @PostMapping("upload")
    public String upload(@RequestPart("file") MultipartFile file) throws IOException {
        return fileStoreService.upload(file).getAccessPath();
    }
}
