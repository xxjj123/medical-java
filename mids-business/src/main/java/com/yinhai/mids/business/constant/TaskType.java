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
    COMPUTE_RESULT(2),

    /**
     * MPR建模
     */
    MPR(3),

    /**
     * 科亚AI分析申请
     */
    KEYA_APPLY(4),

    /**
     * 科亚AI分析结果查询
     */
    KEYA_QUERY(5),

    /**
     * 脊柱识别
     */
    SPINE_RECOG(6),
    ;

    private final int type;

    TaskType(int type) {
        this.type = type;
    }
}
