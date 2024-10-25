package com.yinhai.mids.business.mpr;

import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/6/30 22:16
 */
@Data
public class RegisterParam {

    /**
     * 序列ID
     */
    private String seriesId;

    private String applyId;

    /**
     * AI分析完成回调地址
     */
    private String callbackUrl;

    /**
     * MPR类型，多个用英文逗号拼接。例如 slice,lung
     */
    private String types;
}
