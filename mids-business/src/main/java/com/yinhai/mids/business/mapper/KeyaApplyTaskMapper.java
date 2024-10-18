package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.entity.po.KeyaApplyTaskPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
public interface KeyaApplyTaskMapper extends SuperMapper<KeyaApplyTaskPO> {

    /**
     * 查询待办任务
     *
     * @return {@link List }<{@link KeyaApplyToDoTask }>
     */
    List<KeyaApplyToDoTask> queryTodoTasks();
}
