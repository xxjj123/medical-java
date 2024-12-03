package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.vo.ImageInitInfoVO;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
public interface ImageService {

    /**
     * 查询影像初始化数据
     *
     * @param computeSeriesId computeSeriesId
     * @return {@link ImageInitInfoVO }
     * @author zhuhs 2024/08/27
     */
    ImageInitInfoVO queryInitInfo(String computeSeriesId);

    /**
     * 下载影像切片
     *
     * @param seriesId  seriesId
     * @param viewName  viewName
     * @param viewIndex viewIndex
     * @param response  HttpServletResponse
     * @author zhuhs 2024/08/28
     */
    void downloadSlice(String seriesId, String viewName, Integer viewIndex, HttpServletResponse response);

    /**
     * 下载3D模型
     *
     * @param seriesId seriesId
     * @param response HttpServletResponse
     * @author zhuhs 2024/08/28
     */
    void download3dModel(String seriesId, HttpServletResponse response);

    /**
     * 下载影像DICOM
     */
    void downloadDicom(String instanceId, HttpServletResponse response);
}
