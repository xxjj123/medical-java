package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.SpineRecogToDoTask;
import com.yinhai.mids.business.entity.po.SpineRecogTaskPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/12/2
 */
public interface SpineRecogTaskMapper extends SuperMapper<SpineRecogTaskPO> {

    /**
     * 查询待办任务
     *
     * @return {@link List }<{@link SpineRecogToDoTask }>
     */
    List<SpineRecogToDoTask> queryTodoTasks();
}
