package com.yinhai.mids.common.module.mybatis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.ta404.core.restservice.requestbean.PageParam;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.module.mybatis.mapper.Ta404SupportMapper;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author zhuhs
 * @date 2022/3/9 15:06
 */
public interface SuperMapper<T> extends BaseMapper<T>, Ta404SupportMapper {

    Map<Class<?>, Log> LOG_MAP = new HashMap<>();

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
     * 批量插入
     *
     * @param entityList 实体对象集合
     * @return boolean
     */
    default boolean insertBatch(Collection<T> entityList) {
        return insertBatch(entityList, 1000);
    }

    /**
     * 批量插入或更新
     *
     * @param entityList 实体对象集合
     * @return boolean
     */
    default boolean insertOrUpdateBatch(Collection<T> entityList) {
        return insertOrUpdateBatch(entityList, 1000);
    }

    /**
     * 批量插入
     *
     * @param entityList 实体对象集合
     * @param batchSize  每批数量
     * @return boolean
     */
    default boolean insertBatch(Collection<T> entityList, int batchSize) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        Optional<T> optional = entityList.stream().findFirst();
        return optional.filter(t -> SqlHelper.executeBatch(
                t.getClass(),
                getLog(getMapperInterface()),
                entityList,
                batchSize,
                (sqlSession, entity) -> sqlSession.insert(getSqlStatement(SqlMethod.INSERT_ONE), entity)
        )).isPresent();
    }

    /**
     * 批量插入或更新
     *
     * @param entityList 实体对象集合
     * @param batchSize  每批数量
     * @return boolean
     */
    default boolean insertOrUpdateBatch(Collection<T> entityList, int batchSize) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        Optional<T> optional = entityList.stream().findFirst();
        if (!optional.isPresent()) {
            return false;
        }

        Class<?> entityClass = optional.get().getClass();
        Field updateTime = ReflectUtil.getField(entityClass, "updateTime");
        Field createTime = ReflectUtil.getField(entityClass, "createTime");
        for (T t : entityList) {
            if (updateTime != null && updateTime.getType().equals(Date.class)) {
                ReflectUtil.setAccessible(updateTime);
                ReflectUtil.setFieldValue(t, updateTime, MapperKit.executeForDate());
            }
            if (createTime != null && createTime.getType().equals(Date.class)) {
                if (ReflectUtil.getFieldValue(t, createTime) == null) {
                    ReflectUtil.setAccessible(createTime);
                    ReflectUtil.setFieldValue(t, createTime, MapperKit.executeForDate());
                }
            }
        }

        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
        Class<?> mapperClass = getMapperInterface();
        return SqlHelper.saveOrUpdateBatch(
                entityClass,
                mapperClass,
                getLog(mapperClass),
                entityList,
                batchSize,
                (sqlSession, entity) -> {
                    Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
                    return StringUtils.checkValNull(idVal)
                           || CollectionUtils.isEmpty(
                            sqlSession.selectList(getSqlStatement(SqlMethod.SELECT_BY_ID), entity));
                }, (sqlSession, entity) -> {
                    MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
                    param.put(Constants.ENTITY, entity);
                    sqlSession.update(getSqlStatement(SqlMethod.UPDATE_BY_ID), param);
                });
    }

    /**
     * 获取mapperStatementId
     *
     * @param sqlMethod 方法名
     * @return {@link String }
     */
    default String getSqlStatement(SqlMethod sqlMethod) {
        return SqlHelper.getSqlStatement(getMapperInterface(), sqlMethod);
    }

    /**
     * 获取MapperInterface
     *
     * @return {@link Class }<{@link ? }>
     * @author zhuhs 2022/07/19 08:56
     */
    default Class<?> getMapperInterface() {
        if (this instanceof Proxy) {
            // 如果是动态代理的，获取被代理的mapper class
            return (Class<?>) ReflectUtil.getFieldValue(ReflectUtil.getFieldValue(this, "h"), "mapperInterface");
        }
        return this.getClass();
    }

    /**
     * 获取不同mp mapper的log对象
     *
     * @param clazz mp mapper class
     * @return {@link Log }
     * @author zhuhs 2022/09/07 14:54
     */
    static Log getLog(Class<?> clazz) {
        return LOG_MAP.computeIfAbsent(clazz, LogFactory::getLog);
    }
}
