package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.po.InstanceInfoPO;

import java.io.File;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/21
 */
public interface InstanceInfoService {

    File readDicom(List<InstanceInfoPO> instanceInfoList);
}
