package com.yinhai.mids.business.event;

import com.google.common.collect.Multimap;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.service.InstanceService;
import com.yinhai.ta404.core.event.async.AbstractEventHandler;
import com.yinhai.ta404.core.event.async.annotation.Consumer;
import com.yinhai.ta404.core.event.async.disruptor.IEventDisruptor;
import com.yinhai.ta404.core.event.async.message.IEventMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/9 10:08
 */
@Consumer(value = EventConstants.DICOM_UPLOAD, async = true)
@Component
public class DicomUploadEventHandler implements AbstractEventHandler {

    @Resource
    private InstanceService instanceService;

    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(IEventDisruptor event, long sequence, boolean endOfBatch) throws Exception {
        IEventMessage eventMessage = event.getEventMessage();
        Map<String, Object> eventSource = (Map<String, Object>) eventMessage.getEventSource();
        List<String> studyIdList = (List<String>) eventSource.get("studyIdList");
        Multimap<String, DicomInfo> seriesMap = (Multimap<String, DicomInfo>) eventSource.get("seriesMap");
        instanceService.handleInstances(studyIdList, seriesMap);
    }
}
