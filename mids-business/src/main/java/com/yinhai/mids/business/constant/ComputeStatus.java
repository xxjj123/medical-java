package com.yinhai.mids.business.constant;

/**
 * @author zhuhs
 * @date 2024/7/10 14:18
 */
public class ComputeStatus {

    /**
     * 等待计算
     */
    public static final int WAIT_COMPUTE = 1;
    /**
     * 计算中
     */
    public static final int IN_COMPUTE = 2;
    /**
     * 计算成功
     */
    public static final int COMPUTE_SUCCESS = 3;
    /**
     * 计算失败
     */
    public static final int COMPUTE_FAILED = 4;
    /**
     * 计算取消
     */
    public static final int COMPUTE_CANCELED = 5;
    /**
     * 计算异常
     */
    public static final int COMPUTE_ERROR = 6;
}
