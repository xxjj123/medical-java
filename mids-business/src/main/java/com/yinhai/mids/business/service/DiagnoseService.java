package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.vo.FocalVO;
import com.yinhai.mids.business.entity.vo.SeriesVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
public interface DiagnoseService {
    /**
     * 获取结节信息
     *
     */
    FocalVO getNoduleInfo(String computeSeriesId);


    /**
     * 读取dicom压缩包
     *
     */
    InputStream downloadDicomZip(String computeSeriesId);

    InputStream downSlice(String seriesId,String viewName,Integer viewIndex);

    void onMprPush(MultipartFile vtiZip,MultipartFile glbZip,String computeSeriesId,String code,String message)  throws IOException;


    SeriesVO getSeriesInfo(String computeSeriesId);
}
