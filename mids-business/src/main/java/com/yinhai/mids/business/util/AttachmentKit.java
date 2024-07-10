package com.yinhai.mids.business.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.entity.po.AttachmentPO;
import com.yinhai.mids.business.entity.po.FileStorePO;
import com.yinhai.mids.business.entity.vo.AttachmentVO;
import com.yinhai.mids.business.mapper.AttachmentMapper;
import com.yinhai.mids.business.mapper.FileStoreMapper;
import com.yinhai.ta404.core.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuhs
 * @date 2024/7/9 17:04
 */
public class AttachmentKit {

    public static List<AttachmentVO> getAttachments(String objectId, int useType) {
        AttachmentMapper attachmentMapper = ServiceLocator.getService(AttachmentMapper.class);
        LambdaQueryWrapper<AttachmentPO> q = Wrappers.<AttachmentPO>lambdaQuery()
                .eq(AttachmentPO::getObjectId, objectId)
                .eq(AttachmentPO::getUseType, useType);
        List<AttachmentPO> attachmentPOList = attachmentMapper.selectList(q);
        if (CollUtil.isEmpty(attachmentPOList)) {
            return new ArrayList<>();
        }
        FileStoreMapper fileStoreMapper = ServiceLocator.getService(FileStoreMapper.class);
        List<String> storeIdList = attachmentPOList.stream().map(AttachmentPO::getStoreId).collect(Collectors.toList());
        List<FileStorePO> fileStorePOList;
        if (CollUtil.size(storeIdList) > 1) {
            fileStorePOList = fileStoreMapper.selectList(
                    Wrappers.<FileStorePO>lambdaQuery().in(FileStorePO::getId, storeIdList));
        } else {
            fileStorePOList = CollUtil.toList(fileStoreMapper.selectById(storeIdList.get(0)));
        }
        if (CollUtil.isEmpty(fileStorePOList)) {
            return new ArrayList<>();
        }
        List<AttachmentVO> attachmentVOList = new ArrayList<>();
        for (FileStorePO fileStorePO : fileStorePOList) {
            AttachmentVO vo = new AttachmentVO();
            vo.setStoreId(fileStorePO.getId());
            vo.setObjectId(objectId);
            vo.setUseType(useType);
            vo.setSize(fileStorePO.getSize());
            vo.setContentType(fileStorePO.getContentType());
            vo.setOriginalName(fileStorePO.getOriginalName());
            vo.setAccessPath(fileStorePO.getAccessPath());
            attachmentVOList.add(vo);
        }
        return attachmentVOList;
    }

}
