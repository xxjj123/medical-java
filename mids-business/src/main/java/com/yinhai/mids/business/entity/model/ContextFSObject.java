package com.yinhai.mids.business.entity.model;

import com.yinhai.ta404.module.storage.core.TaFSObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuhs
 * @date 2024/7/2 15:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContextFSObject<T> extends TaFSObject {

    private T context;

}
