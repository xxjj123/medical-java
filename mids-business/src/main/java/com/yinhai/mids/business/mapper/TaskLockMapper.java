package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.po.TaskLockPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/9/4
 */
public interface TaskLockMapper extends SuperMapper<TaskLockPO> {

    int createLock(@Param("id") String id, @Param("itemId") String itemId, @Param("taskType") int taskType,
                   @Param("expireTime") Date expireTime);
}
