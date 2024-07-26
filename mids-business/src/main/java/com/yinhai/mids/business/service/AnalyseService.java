package com.yinhai.mids.business.service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author zhuhs
 * @date 2024/7/18 15:23
 */
public interface AnalyseService {

    void uploadVti(String viewName, int viewIndex, File vtiFile);

    void view(String id, HttpServletResponse response);

    void doMprAnalyse(String seriesId);
}
