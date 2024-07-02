package com.yinhai.mids.common.constant;

import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * @author zhuhs
 * @date 2022/09/22 14:44
 */
public class Constants {

    private Constants() {
        // 私有构造函数，防止被实例化
    }

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 未知
     */
    public static final String UNKNOWN = "_UNKNOWN_";

    /**
     * 图片格式
     */
    public static final String PNG = "PNG";

    public static final String BASE64_IMAGE = "data:image/*;base64,";

    public static final String ZERO = "0";

    public static final String ONE = "1";

    public static final String TWO = "2";

    public static final Integer INT1 = 1;

    public static final Integer INT0 = 0;
    /**
     * 很久之前
     */
    public static final Date SUPER_BEFORE_DATE = DateUtil.parseDateTime("1000-01-01 00:00:00");
    /**
     * 很久以后
     */
    public static final Date SUPER_AFTER_DATE = DateUtil.parseDateTime("3000-01-01 00:00:00");
}
