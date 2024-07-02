package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
public interface FileStoreService {

    UploadVO upload(MultipartFile mf) throws IOException;

}
