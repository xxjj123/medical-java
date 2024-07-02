package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/1 22:51
 */
@Component
public class MybatisRunner implements ApplicationRunner {

    private final List<SqlSessionFactory> sqlSessionFactoryList;

    private final MetaObjectHandler metaObjectHandler;

    public MybatisRunner(List<SqlSessionFactory> sqlSessionFactoryList, MetaObjectHandler metaObjectHandler) {
        this.sqlSessionFactoryList = sqlSessionFactoryList;
        this.metaObjectHandler = metaObjectHandler;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
            globalConfig.setMetaObjectHandler(metaObjectHandler);
        }
    }
}
