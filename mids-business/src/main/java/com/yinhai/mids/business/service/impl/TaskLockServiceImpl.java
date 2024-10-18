package com.yinhai.mids.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.po.TaskLockPO;
import com.yinhai.mids.business.mapper.TaskLockMapper;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.mids.common.util.DbClock;
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

    private static final Log log = LogFactory.get();

    @Resource
    private TaskLockMapper taskLockMapper;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean tryLock(TaskType taskType, String itemId, int expireSeconds) {
        try {
            TaskLockPO taskLockPO = taskLockMapper.selectOne(Wrappers.<TaskLockPO>lambdaQuery()
                    .eq(TaskLockPO::getTaskType, taskType.getType())
                    .eq(TaskLockPO::getItemId, itemId));
            Date now = DbClock.now();
            Date expireTime = expireSeconds <= 0 ? null : DateUtil.offsetSecond(now, expireSeconds);
            if (taskLockPO == null) {
                String id = IdUtil.getSnowflakeNextIdStr();
                int created = taskLockMapper.createLock(id, itemId, taskType.getType(), expireTime);
                return created > 0;
            }

            // 不会失效或者尚未失效
            if (taskLockPO.getExpireTime() == null || taskLockPO.getExpireTime().after(now)) {
                return false;
            }

            int updated = taskLockMapper.update(new TaskLockPO(), Wrappers.<TaskLockPO>lambdaUpdate()
                    .eq(TaskLockPO::getId, taskLockPO.getId())
                    .set(TaskLockPO::getExpireTime, expireTime));
            return updated > 0;
        } catch (Exception e) {
            log.debug("tryLock exception occurred: {}", e.getClass().getName());
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void unlock(TaskType taskType, String itemId) {
        taskLockMapper.delete(Wrappers.<TaskLockPO>lambdaQuery()
                .eq(TaskLockPO::getTaskType, taskType.getType())
                .eq(TaskLockPO::getItemId, itemId));
    }
}
