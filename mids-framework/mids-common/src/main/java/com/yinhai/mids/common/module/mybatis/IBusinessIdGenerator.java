package com.yinhai.mids.common.module.mybatis;

/**
 * @author zhuhs
 * @date 2024/10/31
 */
public interface IBusinessIdGenerator {

    String nextId(Object entity);
}
