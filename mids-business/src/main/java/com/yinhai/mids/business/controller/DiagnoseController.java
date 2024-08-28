package com.yinhai.mids.business.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.vo.FocalVO;
import com.yinhai.mids.business.entity.vo.SeriesVO;
import com.yinhai.mids.business.service.DiagnoseService;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/7/2 9:15
 */
@Validated
@Tag(name = "诊断结果")
@RestService("diagnose")
public class DiagnoseController {

    private static final Log log = LogFactory.get();

    @Resource
    private DiagnoseService diagnoseService;

    @Operation(summary = "序列信息查询")
    @PostMapping("getSeriesInfo")
    public SeriesVO getSeriesInfo(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return diagnoseService.getSeriesInfo(computeSeriesId);
    }

    @Operation(summary = "结节诊断信息查询")
    @PostMapping("getNoduleInfo")
    public FocalVO getNodule(@RequestParam @NotBlank(message = "序列ID不能为空") String computeSeriesId) {
        return diagnoseService.getNoduleInfo(computeSeriesId);
    }

    @Operation(summary = "下载单张切片")
    @PostMapping("getSlice")
    public ResponseEntity<InputStreamResource> getSlice(@RequestParam("seriesId") @NotBlank(message = "序列ID不能为空") String seriesId,
                                                        @RequestParam("viewName") @NotBlank(message = "切片方向不能为空") String viewName,
                                                        @RequestParam("viewIndex")  @NotNull(message = "切片序列不能为空") Integer viewIndex) {
        try{
            InputStream inputStream=diagnoseService.downSlice(seriesId,viewName,viewIndex);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test" );
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            log.error(e);
            if(e instanceof AppException){
                throw new AppException("下载切片失败！"+((AppException) e).getErrorMessage());

            }
            throw new AppException("下载切片失败！" );
        }
    }

    @Operation(summary = "下载3d模型")
    @PostMapping("getModel3d")
    public ResponseEntity<InputStreamResource> getModel3d(@RequestParam("seriesId") @NotBlank(message = "序列ID不能为空") String seriesId) {
        try{
            InputStream inputStream=diagnoseService.downModel3d(seriesId);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test" );
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            log.error(e);
            if(e instanceof AppException){
                throw new AppException("下载模型失败！"+((AppException) e).getErrorMessage());

            }
            throw new AppException("下载模型失败！" );
        }
    }
}
