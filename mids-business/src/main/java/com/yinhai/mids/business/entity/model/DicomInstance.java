package com.yinhai.mids.business.entity.model;

import com.yinhai.mids.business.entity.po.InstancePO;
import lombok.Data;

import java.io.File;

/**
 * @author zhuhs
 * @date 2024/7/2 11:10
 */
@Data
public class DicomInstance {

    private DicomInfo dicomInfo;

    private File dicomFile;

    private InstancePO instance;

}
