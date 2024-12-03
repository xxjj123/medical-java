package com.yinhai.mids.common.module.mybatis;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.ta404.core.restservice.requestbean.PageParam;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.module.mybatis.mapper.Ta404SupportMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuhs
 * @date 2022/3/9 15:06
 */
public interface SuperMapper<T> extends BaseMapper<T>, Ta404SupportMapper {

    /**
     * 开启分页
     *
     * @param pageParam 分页参数
     * @author zhuhs 2022/06/23 15:34
     */
    @Override
    default void beginPager(PageParam pageParam) {
        PageKit.startPage(BeanUtil.copyProperties(pageParam, PageRequest.class));
    }

    /**
     * 结束分页，并转换为目标实体类型
     *
     * @param sourceList  源数据列表
     * @param targetClass 目标实体类型
     * @return {@link Page }<{@link Target }>
     * @author zhuhs 2022/06/23 15:39
     */
    default <Source, Target> Page<Target> endPager(List<Source> sourceList, Class<Target> targetClass) {
        return PageKit.finishPage(sourceList, targetClass);
    }

    /**
     * 根据 whereEntity 条件，更新 entity 调用了 setter 的字段，无论值是否为 null。entity 对象应通过 {@link UpdateEntity} 生成，否则无效
     *
     * @param entity        实体对象 (set 条件值,可以为 null,当entity为null时,无法进行自动填充)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
    int updateSetterInvoked(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);

    /**
     * 根据 ID 查询指定的列。例如
     * <pre> {@code
     * User user = userMapper.selectColumnsById(1, Wrappers.<User>lambdaQuery().select(User::getName, User::getAge));
     * 或
     * User user = userMapper.selectColumnsById(1, Columns.of(User::getName, User::getAge));
     * }</pre>
     *
     * @param id           主键ID
     * @param queryWrapper 用于设置指定的列，其它配置不生效
     */
    T selectColumnsById(@Param(SelectColumnsById.ID) Serializable id, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
}
