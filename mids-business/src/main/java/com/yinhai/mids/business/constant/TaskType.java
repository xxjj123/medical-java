package com.yinhai.mids.business.constant;

import lombok.Getter;

/**
 * 任务任务类型
 *
 * @author zhuhs
 * @date 2024/9/4
 */
@Getter
public enum TaskType {

    /**
     * AI计算
     */
    COMPUTE(1),
    /**
     * 查询AI计算结果
     */
    QUERY_COMPUTE_RESULT(2),
    ;

    private final int type;

    TaskType(int type) {
        this.type = type;
    }
}
