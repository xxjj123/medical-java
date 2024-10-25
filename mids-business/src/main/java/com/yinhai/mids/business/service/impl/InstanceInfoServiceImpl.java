package com.yinhai.mids.business.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.po.InstanceInfoPO;
import com.yinhai.mids.business.service.InstanceInfoService;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhuhs
 * @date 2024/10/21
 */
@Service
@TaTransactional
public class InstanceInfoServiceImpl implements InstanceInfoService {

    private static final Log log = LogFactory.get();


    @Resource
    private ITaFSManager<FSManager> fsManager;

    /**
     * 读取DICOM文件
     */
    @Override
    public File readDicom(List<InstanceInfoPO> instanceInfoList) {
        // 创建临时文件用于压缩DICOM文件
        File tempZip;
        try {
            tempZip = Files.createTempFile(null, ".zip").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZip.toPath()))) {
            for (InstanceInfoPO instanceInfo : instanceInfoList) {
                TaFSObject fsObject = fsManager.getObject("mids", instanceInfo.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instanceInfo.getSopInstanceUid()));
                try (InputStream inputStream = fsObject.getInputstream()) {
                    IoUtil.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return tempZip;
        } catch (IOException e) {
            FileUtil.del(tempZip);
            log.error(e);
            throw new AppException("读取并压缩DICOM文件异常");
        }
    }
}
