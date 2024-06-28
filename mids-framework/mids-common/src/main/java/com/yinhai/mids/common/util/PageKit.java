package com.yinhai.mids.common.util;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.ta404.core.restservice.resultbean.Page;

import java.util.List;

/**
 * 分页工具类
 *
 * @author zhuhs
 * @date 2022/09/22 14:29
 */
public class PageKit {

    /**
     * 开始分页
     *
     * @param pageRequest 分页请求参数
     * @author zhuhs 2022/09/22 15:15
     */
    @SuppressWarnings("all")
    public static void startPage(PageRequest pageRequest) {
        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        if (pageNumber > 1 && pageRequest.isCountOptimize()) {
            // 查询总数优化情况下，页码大于1时，不再统计总数
            PageHelper.startPage(pageNumber, pageSize, false).setReasonable(pageRequest.isReasonable());
        } else {
            PageHelper.startPage(pageNumber, pageSize, true).setReasonable(pageRequest.isReasonable());
        }
    }

    /**
     * 结束分页
     *
     * @param list 结果集合
     * @return {@link Page }<{@link T }>
     * @author zhuhs 2022/09/22 15:26
     */
    public static <T> Page<T> finishPage(List<T> list) {
        Page<T> page = new Page<>();
        PageInfo<T> pageHelperPageInfo = new PageInfo<>(list);
        page.setList(pageHelperPageInfo.getList());
        page.setPageNum(pageHelperPageInfo.getPageNum());
        page.setPages(pageHelperPageInfo.getPages());
        page.setPageSize(pageHelperPageInfo.getPageSize());
        page.setCurrentSize(pageHelperPageInfo.getSize());
        page.setTotal(pageHelperPageInfo.getTotal());
        return page;
    }

    /**
     * 结束分页，并转换为目标实体类型
     *
     * @param sourceList  源数据列表
     * @param targetClass 目标实体类型
     * @return {@link Page }<{@link Target }>
     * @author zhuhs 2022/06/23 15:39
     */
    public static <Source, Target> Page<Target> finishPage(List<Source> sourceList, Class<Target> targetClass) {
        Page<Source> sourcePage = finishPage(sourceList);
        Page<Target> page = new Page<>();
        page.setPageIndexList(sourcePage.getPageIndexList());
        page.setPageNum(sourcePage.getPageNum());
        page.setPageSize(sourcePage.getPageSize());
        page.setCurrentSize(sourcePage.getCurrentSize());
        page.setTotal(sourcePage.getTotal());
        page.setPages(sourcePage.getPages());
        page.setList(BeanUtil.copyToList(sourceList, targetClass));
        return page;
    }
}
