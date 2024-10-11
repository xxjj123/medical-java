package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author zhuhs
 * @date 2024/10/11
 */
public class UpdateSetterInvoked extends AbstractMethod {

    public UpdateSetterInvoked() {
        this("updateSetterInvoked");
    }

    /**
     * @param methodName 方法名
     * @since 3.5.0
     */
    protected UpdateSetterInvoked(String methodName) {
        super(methodName);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.UPDATE;
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
                sqlSet(true, true, tableInfo, true, ENTITY, ENTITY_DOT),
                sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        int subIndex = sql.indexOf("<if test=\"et != null\">") + "<if test=\"et != null\">".length() + 1;
        sql = sql.substring(0, subIndex) + "<foreach item=\"c\" collection=\"et.updateNullColumns\">${c} = null,</foreach>\n" + sql.substring(subIndex);
        SqlSource sqlSource = super.createSqlSource(configuration, sql, modelClass);
        return this.addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
    }
}
