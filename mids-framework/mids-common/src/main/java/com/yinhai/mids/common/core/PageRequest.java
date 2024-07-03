package com.yinhai.mids.common.core;

import com.yinhai.ta404.core.restservice.requestbean.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 分页请求参数
 *
 * @author zhuhs
 * @date 2022/09/22 14:38
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "分页参数")
public class PageRequest extends PageParam {

    /**
     * 分页参数合理化
     */
    private boolean reasonable = true;

    private static PageRequest of(int pageNumber, int pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNumber(pageNumber);
        pageRequest.setPageSize(pageSize);
        return pageRequest;
    }
}
