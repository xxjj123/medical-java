package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.MprPushParam;
import com.yinhai.mids.business.entity.dto.MprToDoTask;

/**
 * @author zhuhs
 * @date 2024/7/18 15:23
 */
public interface MprService {

    void mpr(MprToDoTask mprTask);

    void lockedAsyncMpr(MprToDoTask mprTask);

    void onMprPush(MprPushParam mprPushParam);
}
