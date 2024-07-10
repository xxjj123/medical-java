package com.yinhai.mids.business.service.impl;

import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.FileStorePO;
import com.yinhai.mids.business.mapper.FileStoreMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
@Service
@TaTransactional
public class FileStoreServiceImpl implements FileStoreService {

    @Resource
    private FileStoreMapper fileStoreMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

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
        List<FileStorePO> fileStorePOList = new ArrayList<>();
        for (ContextFSObject<T> fsObject : fsObjects) {
            TaFSObject taFSObject = fsManager.putObject("mids", fsObject);
            fsObject.setKeyId(taFSObject.getKeyId());
            FileStorePO fileStorePO = new FileStorePO();
            fileStorePO.setSize(fsObject.getSize());
            fileStorePO.setContentType(fsObject.getContentType());
            fileStorePO.setOriginalName(fsObject.getName());
            fileStorePO.setAccessPath(fsObject.getKeyId());
            fileStorePO.setUploadTime(MapperKit.executeForDate());
            fileStorePOList.add(fileStorePO);
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
