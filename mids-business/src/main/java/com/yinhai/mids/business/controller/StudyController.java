package com.yinhai.mids.business.controller;

import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.business.service.StudyService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/2 9:15
 */
@Tag(name = "检查")
@RestService("study")
public class StudyController {

    @Resource
    private StudyService studyService;

    @Operation(summary = "dicom文件上传")
    @PostMapping("uploadDicom")
    public void uploadDicom(@RequestPart("dicom") MultipartFile dicom) throws IOException {
        studyService.uploadDicom(dicom);
    }

    @Operation(summary = "分页查询检查")
    @GetMapping("page")
    public Page<StudyPageVO> page(@ParameterObject StudyPageQuery studyPageQuery, @ParameterObject PageRequest pageRequest) {
        return studyService.page(studyPageQuery, pageRequest);
    }

}
