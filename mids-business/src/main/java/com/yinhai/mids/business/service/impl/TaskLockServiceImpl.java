package com.yinhai.mids.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.entity.po.TaskLockPO;
import com.yinhai.mids.business.mapper.TaskLockMapper;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/9/4
 */
@Service
@TaTransactional
public class TaskLockServiceImpl implements TaskLockService {

    @Resource
    private TaskLockMapper taskLockMapper;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean tryLock(TaskType taskType, String itemId, int expireSeconds) {
        TaskLockPO taskLockPO = taskLockMapper.selectOne(Wrappers.<TaskLockPO>lambdaQuery()
                .eq(TaskLockPO::getTaskType, taskType.getType())
                .eq(TaskLockPO::getItemId, itemId));
        if (taskLockPO == null) {
            taskLockMapper.createLock(IdUtil.getSnowflakeNextIdStr(), itemId, taskType.getType());
            taskLockPO = taskLockMapper.selectOne(Wrappers.<TaskLockPO>lambdaQuery()
                    .eq(TaskLockPO::getTaskType, taskType.getType())
                    .eq(TaskLockPO::getItemId, itemId));
        }
        Date now = MapperKit.executeForDate();

        // 已锁住并且未失效
        if (BooleanUtil.isTrue(taskLockPO.getEffective())
            && (taskLockPO.getExpireTime() == null || taskLockPO.getExpireTime().after(now))) {
            return false;
        }

        taskLockPO.setEffective(true);
        taskLockPO.setEffectiveTime(now);
        taskLockPO.setExpireTime(expireSeconds <= 0 ? null : DateUtil.offsetSecond(now, expireSeconds));
        int updated = taskLockMapper.updateById(taskLockPO);
        return updated > 0;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void unlock(TaskType taskType, String itemId) {
        taskLockMapper.update(new TaskLockPO(), Wrappers.<TaskLockPO>lambdaUpdate()
                .eq(TaskLockPO::getTaskType, taskType.getType())
                .eq(TaskLockPO::getItemId, itemId)
                .set(TaskLockPO::getEffective, false));
    }
}
