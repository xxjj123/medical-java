package com.yinhai.mids.business.mpr;

import lombok.Data;

/**
 * @author zhuhs
 * @date 2024/6/30 22:16
 */
@Data
public class RegisterParam {

    /**
     * 序列号 必须
     */
    String seriesId;

    /**
     * AI分析完成回调地址
     */
    String callbackUrl;

}
