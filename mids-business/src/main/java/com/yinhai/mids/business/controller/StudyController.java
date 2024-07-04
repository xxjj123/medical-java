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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/2 9:15
 */
@Validated
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
    @GetMapping("pageStudies")
    public Page<StudyPageVO> pageStudies(@ParameterObject StudyPageQuery studyPageQuery, @ParameterObject PageRequest pageRequest) {
        return studyService.pageStudies(studyPageQuery, pageRequest);
    }

    @Operation(summary = "收藏")
    @PostMapping("addFavorite")
    public void addFavorite(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        studyService.addFavorite(studyId);
    }

    @Operation(summary = "取消收藏")
    @PostMapping("removeFavorite")
    public void removeFavorite(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        studyService.removeFavorite(studyId);
    }

    @Operation(summary = "删除检查")
    @PostMapping("deleteStudy")
    public void deleteStudy(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        studyService.deleteStudy(studyId);
    }

    @Operation(summary = "删除序列")
    @PostMapping("deleteSeries")
    public void deleteSeries(@RequestParam @NotBlank(message = "序列ID不能为空") String seriesId) {
        studyService.deleteSeries(seriesId);
    }

}
