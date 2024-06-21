package com.yinhai.ta404.autoconfigure;

import com.yinhai.ta404.core.CommonConstants;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 自动配置类
 * @author zhangjie [zhangjie02@yinhai.com]
 * @since 5.1
 */

@Component
public class AutoConfigure implements ApplicationListener<ApplicationPreparedEvent> {

	private static final List<String> repeatUrl = Arrays.asList("/org/orguser/userManagementRestService/queryEffectiveUser",
			"/org/authority/authorityAgentRestService/queryReAgentUsersByOrgId",
			"/codetable/getCode",
			"/indexRestService/getCurUserAccount",
			"/org/orguser/orgManagementRestService/getOrgByAsync",
			"/org/authority/roleAuthorityManagementRestService/queryCurrentAdminRoleWrapeOrgTree",
			"/org/authority/examinerAuthorityRestService/queryOrgTreeByAsync");

	private static final Set<String> xssWriteUrlList = new HashSet<>(Arrays.asList("/tasysconfig/taSysConfigRestService/updateSysConfig"
			,"/tasysconfig/taSysConfigRestService/addSysConfig"
			,"/org/sysmg/manageableFieldsRestService/**"));

	private static final Set<String> xssKeywordUrl = new HashSet<>(Arrays.asList("/message/**"));

    private static final Map<String, Object> needConfig = new HashMap<>();

	static {
		needConfig.put("ta404.modules.captcha.user-check-code", true);
		needConfig.put("ta404.limit.repeat-extra-url", repeatUrl);
		needConfig.put("ta404.modules.websecurity.xss-filter.skip-chars", "()[]{}/-+:;@*#=?%");
		needConfig.put("ta404.modules.websecurity.xss-filter.xss-write-url-list", xssWriteUrlList);
		needConfig.put("ta404.modules.websecurity.xss-filter.only-transfer-xss-keyword-url", xssKeywordUrl);
	}

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		PropertySource source = new MapPropertySource(CommonConstants.CUSTOMAUTOCONFIG, needConfig);
		environment.getPropertySources().addLast(source);
	}
}
