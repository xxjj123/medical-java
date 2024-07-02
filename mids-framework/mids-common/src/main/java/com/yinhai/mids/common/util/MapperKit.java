package com.yinhai.mids.common.util;

import com.yinhai.ta404.core.utils.ServiceLocator;
import com.yinhai.ta404.module.dict.mapper.read.DictReadMapper;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author zhuhs
 * @date 2024/7/1 16:42
 */
public class MapperKit {

    public static Date executeForDate() {
        return new Date(ServiceLocator.getService(DictReadMapper.class).executeForTimestamp().getTime());
    }

    public static Timestamp executeForTimestamp() {
        return ServiceLocator.getService(DictReadMapper.class).executeForTimestamp();
    }

    public static String executeForSequence(String sequence) {
        return ServiceLocator.getService(DictReadMapper.class).executeForSequence(sequence);
    }

    public static int executeForUpdateSequence(String sequence) {
        return ServiceLocator.getService(DictReadMapper.class).executeForUpdateSequence(sequence);
    }
}
