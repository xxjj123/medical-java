package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author zhuhs
 * @date 2024/12/3
 */
public class SelectColumnsById extends AbstractMethod {

    public static final String ID = "id";

    public SelectColumnsById() {
        this("selectColumnsById");
    }

    /**
     * @param methodName 方法名
     * @since 3.5.0
     */
    protected SelectColumnsById(String methodName) {
        super(methodName);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
        SqlSource sqlSource = super.createSqlSource(configuration, String.format("<script>" + sqlMethod.getSql() + "\n</script>",
                sqlSelectColumns(tableInfo, true),
                tableInfo.getTableName(), tableInfo.getKeyColumn(), ID,
                tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }
}
