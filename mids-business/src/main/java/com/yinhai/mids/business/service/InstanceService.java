package com.yinhai.mids.business.service;

import com.google.common.collect.Multimap;
import com.yinhai.mids.business.entity.model.DicomInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/8 16:47
 */
public interface InstanceService {

    void handleInstances(List<String> studyIdList, Multimap<String, DicomInfo> seriesMap) throws IOException;

}
