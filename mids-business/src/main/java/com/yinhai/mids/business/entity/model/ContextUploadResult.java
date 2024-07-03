package com.yinhai.mids.business.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuhs
 * @date 2024/7/2 14:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContextUploadResult<T> extends UploadResult {

    private T context;

}
