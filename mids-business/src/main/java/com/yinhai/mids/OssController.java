package com.yinhai.mids;

import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/6/25 16:57
 */
@RestController
@RequestMapping("oss")
@Tag(name = "OSS")
public class OssController {

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Operation(summary = "上传")
    @PostMapping("upload")
    public String upload(@RequestPart("file") MultipartFile file) throws IOException {
        TaFSObject fs = new TaFSObject();
        fs.setInputstream(new ByteArrayInputStream(file.getBytes()));
        fs.setName(file.getOriginalFilename());
        fs.setSize(file.getSize());
        fs.setContentType(file.getContentType());
        fs = fsManager.putObject("dicom", fs);
        System.out.println(fs.getKeyId());
        return fs.getKeyId();
    }

}
