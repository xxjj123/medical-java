package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Multimap;
import com.yinhai.mids.business.constant.AttachmentType;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.AttachmentPO;
import com.yinhai.mids.business.entity.po.InstancePO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.AttachmentMapper;
import com.yinhai.mids.business.mapper.InstanceMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.mapper.StudyMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.InstanceService;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * @author zhuhs
 * @date 2024/7/8 16:52
 */
@Slf4j
@Service
@TaTransactional
public class InstanceServiceImpl implements InstanceService {

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private AttachmentMapper attachmentMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void handleInstances(List<String> studyIdList, Multimap<String, DicomInfo> seriesMap) throws IOException {
        List<SeriesPO> seriesPOList = seriesMapper.selectList(
                Wrappers.<SeriesPO>lambdaQuery().in(SeriesPO::getStudyId, studyIdList));

        Map<DicomInfo, InstancePO> instanceMap = new HashMap<>();
        List<AttachmentPO> seriesAttachments = new ArrayList<>();
        for (SeriesPO seriesPO : seriesPOList) {
            // 一个序列一个zip
            Path tempFile = Files.createTempFile(seriesPO.getId(), ".zip");
            try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile)) {
                Collection<DicomInfo> dicomInfos = seriesMap.get(seriesPO.getSeriesUid());
                for (DicomInfo dicomInfo : dicomInfos) {
                    InstancePO instancePO = BeanUtil.copyProperties(dicomInfo, InstancePO.class);
                    instancePO.setStudyId(seriesPO.getStudyId());
                    instancePO.setSeriesId(seriesPO.getId());
                    instanceMap.put(dicomInfo, instancePO);

                    ZipArchiveEntry entry = new ZipArchiveEntry(dicomInfo.getFile(), dicomInfo.getFile().getName());
                    zaos.putArchiveEntry(entry);
                    FileUtil.writeToStream(dicomInfo.getFile(), zaos);
                    zaos.closeArchiveEntry();
                }
            }
            UploadResult result = fileStoreService.upload(new ContextFSObject<>(tempFile.toFile()));
            AttachmentPO attachmentPO = new AttachmentPO();
            attachmentPO.setStoreId(result.getStoreId());
            attachmentPO.setObjectId(seriesPO.getId());
            attachmentPO.setUseType(AttachmentType.DICOM_ZIP);
            seriesAttachments.add(attachmentPO);
            FileUtil.del(tempFile);
        }
        attachmentMapper.insertBatch(seriesAttachments);

        instanceMapper.insertBatch(instanceMap.values());

        // 保存单个DICOM文件
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        for (DicomInfo di : instanceMap.keySet()) {
            ContextFSObject<String> contextFSObject = new ContextFSObject<>(di.getFile());
            contextFSObject.setContext(instanceMap.get(di).getId());
            contextFSObjects.add(contextFSObject);
        }
        List<ContextUploadResult<String>> contextUploadResults = fileStoreService.upload(contextFSObjects);
        attachmentMapper.insertBatch(contextUploadResults.stream().map(i -> {
            AttachmentPO attachmentPO = new AttachmentPO();
            attachmentPO.setStoreId(i.getStoreId());
            attachmentPO.setObjectId(i.getContext());
            attachmentPO.setUseType(AttachmentType.DICOM_FILE);
            return attachmentPO;
        }).collect(toList()));

        // 删除临时文件
        FileUtil.del(CollUtil.getFirst(seriesMap.values()).getFile().getParentFile());
    }

}
