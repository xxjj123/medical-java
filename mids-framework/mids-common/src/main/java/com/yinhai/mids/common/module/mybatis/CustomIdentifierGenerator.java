package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.yinhai.ta404.core.utils.ServiceLocator;

/**
 * @author zhuhs
 * @date 2024/10/30
 */
public class CustomIdentifierGenerator implements IdentifierGenerator {

    private final IdentifierGenerator defaultIdentifierGenerator;

    public CustomIdentifierGenerator(IdentifierGenerator defaultIdentifierGenerator) {
        this.defaultIdentifierGenerator = defaultIdentifierGenerator;
    }


    @Override
    public Number nextId(Object entity) {
        return defaultIdentifierGenerator.nextId(entity);
    }

    @Override
    public String nextUUID(Object entity) {
        return ServiceLocator.getService(IBusinessIdGenerator.class).nextId(entity);
    }
}
