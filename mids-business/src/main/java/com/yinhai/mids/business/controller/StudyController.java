package com.yinhai.mids.business.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yinhai.mids.business.entity.dto.AlgorithmParam;
import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.business.service.StudyService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.mids.common.util.RestServiceKit;
import com.yinhai.ta404.core.exception.AppException;
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
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/2 9:15
 */
@Validated
@Tag(name = "检查")
@RestService("study")
public class StudyController {

    private static final Log log = LogFactory.get();

    @Resource
    private StudyService studyService;

    @Operation(summary = "dicom文件上传")
    @PostMapping("uploadDicom")
    public void uploadDicom(@RequestPart("dicom")
                            @NotNull(message = "DICOM文件不能为空") MultipartFile dicom,
                            @RequestParam("algorithmConfig")
                            @NotBlank(message = "算法设置不能为空") String algorithmConfig) throws IOException {
        List<AlgorithmParam> algorithmParamList;
        try {
            algorithmParamList = JsonKit.parseObject(algorithmConfig, new TypeReference<List<AlgorithmParam>>() {
            });
        } catch (Exception e) {
            log.error(e, "解析算法设置异常");
            throw new AppException("解析算法设置异常！");
        }
        studyService.uploadDicom(dicom, algorithmParamList);
        RestServiceKit.setData("uploadTime", DateUtil.formatDateTime(DbClock.now()));
    }

    @Operation(summary = "分页查询检查")
    @PostMapping("pageStudies")
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
    public void deleteSeries(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        studyService.deleteComputeSeries(computeSeriesId);
    }

}
