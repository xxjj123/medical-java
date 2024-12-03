package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * @author zhuhs
 * @date 2024/12/3
 */
public class Columns {

    @SafeVarargs
    public static <T> LambdaQueryWrapper<T> of(SFunction<T, ?>... columns) {
        return Wrappers.<T>lambdaQuery().select(columns);
    }
}
