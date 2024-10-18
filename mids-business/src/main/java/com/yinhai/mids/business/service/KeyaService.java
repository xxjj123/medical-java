package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;

import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
public interface KeyaService {

    void apply(KeyaApplyToDoTask applyTask);

    void lockedAsyncApply(KeyaApplyToDoTask applyTask);

    void query(KeyaQueryToDoTask queryTask);

    void lockedAsyncQuery(KeyaQueryToDoTask queryTask);

    void onApplyPush(Map<String, Object> pushParamMap);
}
