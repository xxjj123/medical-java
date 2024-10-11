package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author zhuhs
 * @date 2024/10/11
 */
public class UpdateEntity {

    private static final String GET_NULL_COLUMNS = SetToNullMonitor.class.getMethods()[0].getName();

    private static final Map<Method, String> TABLE_COLUMN_MAP = new ConcurrentHashMap<>();

    private UpdateEntity() {
    }

    /**
     * 生成类的代理对象，该对象可以对自身的set方法进行监控并记录
     */
    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> clazz) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setInterfaces(new Class[]{SetToNullMonitor.class});
        enhancer.setCallback(new UpdateNullColumnsInterceptor());
        return (T) enhancer.create();
    }

    private interface SetToNullMonitor {

        Set<String> getUpdateNullColumns();
    }

    private static class UpdateNullColumnsInterceptor implements MethodInterceptor {

        private final Set<String> updateNullColumns = new CopyOnWriteArraySet<>();

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            if (GET_NULL_COLUMNS.equals(method.getName())) {
                return updateNullColumns;
            }
            Object result = methodProxy.invokeSuper(obj, args);

            String column = TABLE_COLUMN_MAP.get(method);
            if (column != null && args[0] == null) {
                updateNullColumns.add(column);
                return result;
            }

            String methodName = method.getName();
            boolean isSetter = methodName.startsWith("set")
                               && methodName.length() > 3
                               && Character.isUpperCase(methodName.charAt(3))
                               && method.getParameterCount() == 1;
            if (!isSetter || args[0] != null) {
                return result;
            }

            String field = methodName.substring(3).toLowerCase();
            TableInfo tableInfo = TableInfoHelper.getTableInfo(method.getDeclaringClass());
            if (tableInfo == null) {
                return result;
            }
            List<TableFieldInfo> fieldList = tableInfo.getFieldList();
            for (TableFieldInfo tableFieldInfo : fieldList) {
                String property = tableFieldInfo.getProperty();
                if (field.equals(property)) {
                    updateNullColumns.add(tableFieldInfo.getColumn());
                    TABLE_COLUMN_MAP.put(method, tableFieldInfo.getColumn());
                    break;
                }
            }
            return result;
        }
    }
}
