package com.yinhai.mids.business.entity.model;

import cn.hutool.core.io.FileUtil;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/2 15:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContextFSObject<T> extends TaFSObject {

    private T context;

    public ContextFSObject() {
        // default constructor
    }

    public ContextFSObject(MultipartFile mf) throws IOException {
        AppAssert.notNull(mf, "上传文件不能为空");
        AppAssert.notBlank(mf.getOriginalFilename(), "上传文件文件名不能为空");
        AppAssert.isTrue(mf.getOriginalFilename().length() < 200, "上传文件文件名长度不能大于200");
        setInputstream(new ByteArrayInputStream(mf.getBytes()));
        setName(mf.getOriginalFilename());
        setContentType(mf.getContentType());
        setSize(mf.getSize());
    }

    public ContextFSObject(File f) throws IOException {
        AppAssert.notNull(f, "上传文件不能为空");
        AppAssert.notBlank(f.getName(), "上传文件文件名不能为空");
        AppAssert.isTrue(f.getName().length() < 200, "上传文件文件名长度不能大于200");
        setInputstream(FileUtil.getInputStream(f));
        setName(f.getName());
        setContentType(FileUtil.getMimeType(f.toPath()));
        setSize(FileUtil.size(f));
    }

}
