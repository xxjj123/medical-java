package com.yinhai.mids.business.service.impl;

import com.yinhai.mids.business.entity.po.FileStorePO;
import com.yinhai.mids.business.entity.vo.UploadVO;
import com.yinhai.mids.business.mapper.FileStoreMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/1 16:29
 */
@Service
public class FileStoreServiceImpl implements FileStoreService {

    @Resource
    private FileStoreMapper fileStoreMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Override
    public UploadVO upload(MultipartFile mf) throws IOException {
        AppAssert.notNull(mf, "上传文件不能为空");
        AppAssert.notBlank(mf.getOriginalFilename(), "上传文件文件名不能为空");
        AppAssert.isTrue(mf.getOriginalFilename().length() < 200, "上传文件文件名长度不能大于200");

        TaFSObject fs = new TaFSObject();
        fs.setInputstream(new ByteArrayInputStream(mf.getBytes()));
        fs.setName(mf.getOriginalFilename());
        fs.setContentType(mf.getContentType());
        fs = fsManager.putObject("mids", fs);

        FileStorePO fileStorePO = new FileStorePO();
        fileStorePO.setSize(mf.getSize());
        fileStorePO.setContentType(mf.getContentType());
        fileStorePO.setOriginalName(mf.getOriginalFilename());
        fileStorePO.setAccessPath(fs.getKeyId());
        fileStorePO.setUploadTime(MapperKit.executeForDate());
        fileStoreMapper.insert(fileStorePO);

        UploadVO uploadVO = new UploadVO();
        uploadVO.setStoreId(fileStorePO.getId());
        uploadVO.setAccessPath(fs.getKeyId());
        return uploadVO;
    }
}
