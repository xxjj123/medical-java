package com.yinhai.mids.common.module.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yinhai.mids.common.constant.Constants;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.SecurityKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author zhuhs
 * @date 2022/08/27 17:35
 */
@Slf4j
@Component("metaObjectHandler")
public class TableAutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 时间
        Date dbNow = DbKit.now();
        this.strictInsertFill(metaObject, "createTime", Date.class, dbNow);
        this.strictInsertFill(metaObject, "updateTime", Date.class, dbNow);
        // 用户
        String loginId = StrUtil.isBlank(SecurityKit.currentUserId()) ? Constants.UNKNOWN : SecurityKit.currentUserId();
        this.strictInsertFill(metaObject, "createUser", String.class, loginId);
        this.strictInsertFill(metaObject, "updateUser", String.class, loginId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 时间
        Date dbNow = DbKit.now();
        this.strictUpdateFill(metaObject, "updateTime", Date.class, dbNow);
        // 用户
        String loginId = StrUtil.isBlank(SecurityKit.currentUserId()) ? Constants.UNKNOWN : SecurityKit.currentUserId();
        this.strictUpdateFill(metaObject, "updateUser", String.class, loginId);
    }

    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        if (StrUtil.equals("updateTime", fieldName) || metaObject.getValue(fieldName) == null) {
            Object obj = fieldVal.get();
            if (Objects.nonNull(obj)) {
                metaObject.setValue(fieldName, obj);
            }
        }
        return this;
    }
}
