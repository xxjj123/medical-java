package com.yinhai.mids.business.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.VtiPO;
import com.yinhai.mids.business.mapper.VtiMapper;
import com.yinhai.mids.business.service.AnalyseService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zhuhs
 * @date 2024/7/18 15:25
 */
@Service
@TaTransactional
public class AnalyseServiceImpl implements AnalyseService {

    private static final Log log = LogFactory.get();

    @Resource
    private VtiMapper vtiMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void uploadVti(String viewName, int viewIndex, File vtiFile) {
        VtiPO vtiPO = new VtiPO();
        vtiPO.setStudyId("1813835186748583938");
        vtiPO.setSeriesId("1813835186798915586");
        vtiPO.setStudyInstanceUid("1.2.392.200036.9125.2.138612190166.20210407000133");
        vtiPO.setSeriesInstanceUid("1.2.840.113619.2.289.3.168430441.447.1617294423.131.3");
        vtiPO.setViewName(viewName);
        vtiPO.setViewIndex(viewIndex);
        try {
            ContextFSObject<File> fsObject = new ContextFSObject<>(vtiFile);
            fsObject.setContentType("application/octet-stream");
            UploadResult uploadResult = fileStoreService.upload(fsObject);
            vtiPO.setAccessPath(uploadResult.getAccessPath());
        } catch (IOException e) {
            throw new AppException("上传文件异常");
        }
        vtiMapper.insert(vtiPO);
    }

    @Override
    public void view(String id, HttpServletResponse response) {
        VtiPO vtiPO = vtiMapper.selectById(id);
        try (InputStream inputStream = fileStoreService.download(vtiPO.getAccessPath());
             OutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error(e, "下载vti文件异常");
            throw new AppException("文件服务异常");
        }
    }

    @Override
    public void doMprAnalyse(String seriesId) {

    }
}
