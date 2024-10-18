package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;
import com.yinhai.mids.business.entity.po.KeyaQueryTaskPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
public interface KeyaQueryTaskMapper extends SuperMapper<KeyaQueryTaskPO> {

    /**
     * 查询待办任务
     *
     * @return {@link List }<{@link KeyaQueryToDoTask }>
     */
    List<KeyaQueryToDoTask> queryTodoTasks();
}
