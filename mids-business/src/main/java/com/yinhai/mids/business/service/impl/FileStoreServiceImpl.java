package com.yinhai.mids.business.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.FileStorePO;
import com.yinhai.mids.business.mapper.FileStoreMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorCompletionService;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
@Service
@TaTransactional
public class FileStoreServiceImpl implements FileStoreService {

    private static final Log log = LogFactory.get();

    @Resource
    private FileStoreMapper fileStoreMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource(name = "uploadThreadPool")
    private ThreadPoolTaskExecutor uploadThreadPool;

    @Override
    public UploadResult upload(MultipartFile mf) throws IOException {
        return upload(new ContextFSObject<>(mf));
    }

    @Override
    public <T> UploadResult upload(ContextFSObject<T> contextFSObject) throws IOException {
        TaFSObject taFSObject = fsManager.putObject("mids", contextFSObject);
        FileStorePO fileStorePO = new FileStorePO();
        fileStorePO.setSize(contextFSObject.getSize());
        fileStorePO.setContentType(contextFSObject.getContentType());
        fileStorePO.setOriginalName(contextFSObject.getName());
        fileStorePO.setAccessPath(taFSObject.getKeyId());
        fileStorePO.setUploadTime(MapperKit.executeForDate());
        fileStoreMapper.insert(fileStorePO);

        UploadResult uploadResult = new UploadResult();
        uploadResult.setStoreId(fileStorePO.getId());
        uploadResult.setAccessPath(taFSObject.getKeyId());
        return uploadResult;
    }

    @Override
    public <T> List<ContextUploadResult<T>> upload(List<ContextFSObject<T>> fsObjects) {
        List<FileStorePO> fileStorePOList = new CopyOnWriteArrayList<>();

        CompletionService<Void> completionService = new ExecutorCompletionService<>(uploadThreadPool);
        for (ContextFSObject<T> fsObject : fsObjects) {
            completionService.submit(() -> {
                TaFSObject taFSObject = fsManager.putObject("mids", fsObject);
                fsObject.setKeyId(taFSObject.getKeyId());
                FileStorePO fileStorePO = new FileStorePO();
                fileStorePO.setSize(fsObject.getSize());
                fileStorePO.setContentType(fsObject.getContentType());
                fileStorePO.setOriginalName(fsObject.getName());
                fileStorePO.setAccessPath(fsObject.getKeyId());
                fileStorePO.setUploadTime(MapperKit.executeForDate());
                fileStorePOList.add(fileStorePO);
                return null;
            });
        }

        for (ContextFSObject<T> fsObject : fsObjects) {
            try {
                completionService.take().get();
            } catch (Exception e) {
                log.error(e);
                throw new AppException("文件上传失败！");
            }
        }

        fileStoreMapper.insertBatch(fileStorePOList);
        List<ContextUploadResult<T>> contextUploadResultList = new ArrayList<>();
        for (int i = 0; i < fileStorePOList.size(); i++) {
            FileStorePO fileStorePO = fileStorePOList.get(i);
            ContextUploadResult<T> ur = new ContextUploadResult<>();
            ur.setStoreId(fileStorePO.getId());
            ur.setAccessPath(fileStorePO.getAccessPath());
            ur.setContext(fsObjects.get(i).getContext());
            contextUploadResultList.add(ur);
        }
        return contextUploadResultList;
    }
}
