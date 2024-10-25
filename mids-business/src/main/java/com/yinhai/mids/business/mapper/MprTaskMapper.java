package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.MprToDoTask;
import com.yinhai.mids.business.entity.po.MprTaskPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
public interface MprTaskMapper extends SuperMapper<MprTaskPO> {

    /**
     * 查询待办任务
     *
     * @return {@link List }<{@link MprToDoTask }>
     */
    List<MprToDoTask> queryTodoTasks();
}
