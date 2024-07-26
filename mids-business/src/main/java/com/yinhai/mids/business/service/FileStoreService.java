package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
public interface FileStoreService {

    UploadResult upload(MultipartFile mf) throws IOException;

    <T> UploadResult upload(ContextFSObject<T> contextFSObject) throws IOException;

    <T> List<ContextUploadResult<T>> upload(List<ContextFSObject<T>> fsObjects);

    InputStream download(String accessPath);

}
