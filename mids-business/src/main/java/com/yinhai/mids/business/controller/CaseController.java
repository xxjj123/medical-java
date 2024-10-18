package com.yinhai.mids.business.controller;

import cn.hutool.core.date.DateUtil;
import com.yinhai.mids.business.entity.dto.CaseStudyQuery;
import com.yinhai.mids.business.entity.vo.CaseStudyVO;
import com.yinhai.mids.business.service.CaseService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.RestServiceKit;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
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
    public void uploadCaseFile(@RequestPart("caseFile")
                               @NotNull(message = "上传文件不能为空") MultipartFile caseFile) throws IOException {
        caseService.createCase(caseFile);
        RestServiceKit.setData("uploadTime", DateUtil.formatDateTime(DbClock.now()));
    }

    @Operation(summary = "分页查询检查")
    @PostMapping("pageStudies")
    public Page<CaseStudyVO> pageCaseStudies(@ParameterObject CaseStudyQuery caseStudyQuery,
                                             @ParameterObject PageRequest pageRequest) {
        return caseService.pageCaseStudies(caseStudyQuery, pageRequest);
    }

    @Operation(summary = "收藏")
    @PostMapping("addFavorite")
    public void addFavorite(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        caseService.addFavorite(studyId);
    }

    @Operation(summary = "取消收藏")
    @PostMapping("removeFavorite")
    public void removeFavorite(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        caseService.removeFavorite(studyId);
    }

    @Operation(summary = "删除检查")
    @PostMapping("deleteStudy")
    public void deleteCaseStudy(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        caseService.deleteCaseStudy(studyId);
    }

    @Operation(summary = "删除序列")
    @PostMapping("deleteSeries")
    public void deleteCaseSeries(@RequestParam @NotBlank(message = "计算序列ID不能为空") String computeSeriesId) {
        caseService.deleteCaseSeries(computeSeriesId);
    }

    @Operation(summary = "重新分析检查")
    @PostMapping("reComputeStudy")
    public void reComputeStudy(@RequestParam @NotBlank(message = "检查ID不能为空") String studyId) {
        caseService.reComputeStudy(studyId);
    }

    @Operation(summary = "重新分析序列")
    @PostMapping("reComputeSeries")
    public void reComputeSeries(@RequestParam @NotBlank(message = "计算序列ID不能为空") String computeSeriesId) {
        caseService.reComputeSeries(computeSeriesId);
    }
}
