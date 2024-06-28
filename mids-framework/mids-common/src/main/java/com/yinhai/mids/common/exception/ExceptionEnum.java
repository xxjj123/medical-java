package com.yinhai.mids.common.exception;

import com.yinhai.ta404.core.exception.AppException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhuhs
 * @date 2023/11/14 16:20
 */
@Slf4j
public enum ExceptionEnum implements Assertable<AppException>, Exceptional {

    // 框架已经将通用业务错误代码固定为418
    APP(new AppException("").getErrorCode(), "系统异常"),
    ;

    private final String code;

    private final String msg;

    ExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public AppException newException() {
        return new AppException(getMsg());
    }

    @Override
    public AppException newException(String msg) {
        return new AppException(msg);
    }
}
