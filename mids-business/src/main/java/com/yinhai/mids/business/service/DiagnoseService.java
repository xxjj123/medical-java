package com.yinhai.mids.business.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
public interface DiagnoseService {

    InputStream downModel3d(String seriesId);

    void onMprPush(MultipartFile vtiZip, MultipartFile glbZip, String computeSeriesId, String code, String message) throws IOException;
}
