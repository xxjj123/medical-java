package com.yinhai.mids.common.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yinhai.ta404.core.utils.ServiceLocator;
import com.yinhai.ta404.module.dict.mapper.read.DictReadMapper;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/7/1 16:42
 */
public class DbClock {

    private static final LoadingCache<String, Long> TIME_DIFF_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(600L, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String key) throws Exception {
                    return ServiceLocator.getService(DictReadMapper.class).executeForTimestamp().getTime() - new Date().getTime();
                }
            });

    public static Date now() {
        try {
            return new Date(new Date().getTime() + TIME_DIFF_CACHE.get(""));
        } catch (ExecutionException e) {
            return new Date(ServiceLocator.getService(DictReadMapper.class).executeForTimestamp().getTime());
        }
    }
}
