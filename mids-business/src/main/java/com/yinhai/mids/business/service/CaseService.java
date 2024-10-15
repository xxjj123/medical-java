package com.yinhai.mids.business.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/10/14
 */
public interface CaseService {

    /**
     * 创建案例
     */
    void createCase(MultipartFile caseFile) throws IOException;
}
