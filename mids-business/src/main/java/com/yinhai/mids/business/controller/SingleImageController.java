package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.vo.SingleImageInfoVO;
import com.yinhai.mids.business.service.SingleImageService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Validated
@Tag(name = "单张dicom浏览")
@RestService("single")
public class SingleImageController {
    @Resource
    private SingleImageService SingleImageService;

    @Operation(summary = "dicom文件上传")
    @PostMapping("uploadDicom")
    public void uploadDicom(@RequestPart("dicom")
                            @NotNull(message = "DICOM文件不能为空") MultipartFile dicom) throws IOException {
        SingleImageService.uploadDicom(dicom);
    }

    @Operation(summary = "查询影像初始化数据")
    @PostMapping("initInfo")
    public SingleImageInfoVO queryStudyInfo(@RequestParam @NotBlank(message = "序列ID不能为空") String studyid) {
        return SingleImageService.queryInitInfo(studyid);
    }

    @Operation(summary = "下载影像")
    @PostMapping("downloadImage")
    public void downloadSlice(@RequestParam @NotBlank(message = "序列ID不能为空") String studyid,
                              HttpServletResponse response) {
        SingleImageService.downloadSlice(studyid, response);
    }
}


