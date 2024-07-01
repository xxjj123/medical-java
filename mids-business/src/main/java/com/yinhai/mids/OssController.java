package com.yinhai.mids;

import cn.hutool.core.io.IoUtil;
import com.yinhai.ta404.core.validate.annotation.V;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zhuhs
 * @date 2024/6/25 16:57
 */
@RestController
@RequestMapping("oss")
@Tag(name = "OSS")
@Slf4j
@Valid
public class OssController {

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Operation(summary = "上传")
    @PostMapping("upload")
    public String upload(@V({"notnull"}) @RequestPart("file") MultipartFile file) throws IOException {
        TaFSObject fs = new TaFSObject();
        fs.setInputstream(new ByteArrayInputStream(file.getBytes()));
        fs.setName(file.getOriginalFilename());
        fs.setSize(file.getSize());
        fs.setContentType(file.getContentType());
        fs = fsManager.putObject("mids", fs);
        return fs.getKeyId();
    }

    @Operation(summary = "下载")
    @GetMapping("download")
    public void download(@RequestParam String key, HttpServletResponse response) {
        TaFSObject fso = fsManager.getObject("mids", key);
        OutputStream outputStream = null;
        InputStream inputstream = null;
        try {
            outputStream = response.getOutputStream();
            inputstream = fso.getInputstream();
            byte[] bytes = IoUtil.readBytes(inputstream, true);
            response.setContentType(fso.getContentType());
            response.addHeader("Cache-Control", "max-age=1800");
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
