package com.yinhai.mids.common.module.mybatis;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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

    private final List<MybatisPlusInterceptor> mybatisPlusInterceptorList;

    public MybatisRunner(List<SqlSessionFactory> sqlSessionFactoryList,
                         List<MybatisPlusInterceptor> mybatisPlusInterceptorList) {
        this.sqlSessionFactoryList = sqlSessionFactoryList;
        this.mybatisPlusInterceptorList = mybatisPlusInterceptorList;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            for (MybatisPlusInterceptor mybatisPlusInterceptor : mybatisPlusInterceptorList) {
                configuration.addInterceptor(mybatisPlusInterceptor);
                GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
                globalConfig.setIdentifierGenerator(new CustomIdentifierGenerator(globalConfig.getIdentifierGenerator()));
            }
        }
    }
}
