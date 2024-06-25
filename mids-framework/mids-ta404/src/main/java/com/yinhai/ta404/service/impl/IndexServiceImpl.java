package com.yinhai.ta404.service.impl;

import com.yinhai.ta404.component.captcha.core.common.CaptchaTypeEnum;
import com.yinhai.ta404.component.captcha.core.service.CaptchaService;
import com.yinhai.ta404.component.captcha.springboot.starter.properties.CaptchaProperties;
import com.yinhai.ta404.component.security.base.autoconfigure.WebSecurityConfig;
import com.yinhai.ta404.component.security.base.service.EncryptUserService;
import com.yinhai.ta404.component.security.base.service.TaUserAutoUnlockService;
import com.yinhai.ta404.component.security.base.user.TaLockUser;
import com.yinhai.ta404.core.CommonConstants;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.security.vo.UserAccountVo;
import com.yinhai.ta404.core.transaction.annotation.NoTransactional;
import com.yinhai.ta404.core.utils.ValidateUtil;
import com.yinhai.ta404.domain.core.orguserauth.application.event.oplog.OpLogEventPublisher;
import com.yinhai.ta404.domain.core.orguserauth.application.service.command.TaUserCommandAppService;
import com.yinhai.ta404.domain.core.orguserauth.application.service.query.TaUserQueryAppService;
import com.yinhai.ta404.domain.core.orguserauth.domain.aggregate.org.constant.OrgConstant;
import com.yinhai.ta404.domain.core.orguserauth.interfaces.vo.TaUserVo;
import com.yinhai.ta404.module.cache.core.ITaCacheManager;
import com.yinhai.ta404.module.dict.autoconfiger.TaDictProperties;
import com.yinhai.ta404.service.IndexService;
import com.yinhai.ta404.websecurity.service.CryptoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yinhai.ta404.component.core.constant.OpLogEventConstant.*;

/**
 * index service
 *
 * @author MinusZero [hesh@yinhai.com]
 * @since 5.0
 */
@Service
@NoTransactional
public class IndexServiceImpl implements IndexService, ApplicationContextAware {

    @Autowired
    private WebSecurityConfig webSecurityConfig;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private TaUserQueryAppService userManagementReadService;

    @Autowired
    private TaUserCommandAppService userManagementWriteService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private CaptchaProperties captchaProperties;

    @Autowired
    private EncryptUserService encryptUserService;

    @Value("${ta404.component.examine.examine-switch:false}")
    private Boolean isExamineSwitch;

    @Autowired
    private TaDictProperties dictProperties;

    @Autowired
    private TaUserAutoUnlockService taUserAutoUnlockService;

    @Resource(name = "taPasswordEncoder")
    private PasswordEncoder encoder;

    @Resource
    protected OpLogEventPublisher orgOpLogEventPublish;

    private ApplicationContext applicationContext;

    @Resource
    private ITaCacheManager taCacheManager;
    /**
     * 用户自动解锁的缓存信息
     */
    private static final String USER_ERROR_LOGIN_NUM_CACHE = "userErrorLoginNumCache";

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Map<String, Object> getUserInfo(UserAccountVo userAccountVo) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("orgId", userAccountVo.getOrgId());
        map.put("orgName", userAccountVo.getOrgName());
        map.put("userId", userAccountVo.getUserId());
        map.put("loginId", userAccountVo.getLoginId());
        map.put("userName", userAccountVo.getName());
        map.put("sex", userAccountVo.getSex());
        map.put("idCardType", userAccountVo.getIdCardType());
        map.put("idCardNo", userAccountVo.getIdCardNo());
        /*map.put("personRoleId",curUserAccountVo.getPersonRoleId());*/
        map.put("mainRoleId", userAccountVo.getMainRoleId());
        map.put("roles", userAccountVo.getRoles());
        userAccountVo.getRoles().forEach((vo) -> {
            if (OrgConstant.ROLE_TYPE_ADMIN.equals(vo.getRoleType())) {
                map.put("isManager", true);
            }
            if (userAccountVo.getMainRoleId().equals(vo.getRoleId())) {
                map.put("mainRoleName", vo.getRoleName());
            }
        });
        map.put("avatar", userManagementReadService.queryAvatarNocheck(userAccountVo.getUserId()));
        return map;
    }

    @Override
    public void changePassByUserId(String oldPassword, String newPassword, String userId) {
        TaUserVo vo = userManagementReadService.queryUserVoByUserId(userId);
        updatePass(null, vo, oldPassword, newPassword);
    }

    @Override
    public void changePassByLoginId(HttpServletRequest request, String loginId, String oldPassword, String newPassword) {
        TaUserVo vo = userManagementReadService.queryUserVoByLoginId(loginId);
        updatePass(request, vo, oldPassword, newPassword);
    }

    @Override
    public int getPassErrorNum(String loginId) {
        Cache cache = taCacheManager.getCache(CommonConstants.PW_VALIDATION_ERROR_NUMBER);
        UserAccountVo userAccountVo = userManagementReadService.queryUserForPasswordDefaultNum(loginId);
        if (null != userAccountVo) {
            int num = null == userAccountVo.getPasswordDefaultNum() ? 0 : userAccountVo.getPasswordDefaultNum();
            //checkUser 密码错误次数 更新到缓存
            cache.put(loginId,num);
            return num;
        }
        //modify by kangdw:依据等保3三需求，防止用户猜测账户信息，查不到用户时不提示账户不存在
        //密码错误次数
        Integer errorNum = cache.get(loginId, Integer.class);
        return errorNum == null ? 0 : errorNum;

    }

    @Override
    public int getPasswordLevel() {
        return webSecurityConfig.getPasswordLevel();
    }

    @Override
    public boolean isEncryptLoginId() {
        return webSecurityConfig.isEncryptLoginId();
    }

    @Override
    public Map<String, Object> getConfig(HttpServletRequest request) {
        Map<String, Object> configMap = new HashMap<>(12);
        //update by wanggan,去除rsa的配置,修改为是否加密登录账号,以及新增密码安全级别的字段,适用于5.2.0版本及以后
//        configMap.put("passwordRSA", webSecurityConfig.isPasswordRSA());
        configMap.put("passwordLevel", webSecurityConfig.getPasswordLevel());
        configMap.put("encryptLoginId", webSecurityConfig.isEncryptLoginId());
        configMap.put("userCheckCode", captchaProperties.isUserCheckCode());
        configMap.put("checkCodeType", captchaProperties.getCaptchaType());
        configMap.put("numberCheckCodeLevel", captchaProperties.getNumberCheckCodeLevel());
        configMap.put("passwordValidationErrorNumber", captchaProperties.getPasswordValidationErrorNumber());
        configMap.put("isExamineSwitch", isExamineSwitch);
        configMap.put("sessionPasswordErrorNumber", 0);
        configMap.put("openSocialLogin", ifHaveBean("socialConfigApply"));
        configMap.put("openSmsLogin", ifHaveBean("smsConfigApply"));
        configMap.put("openMockUser", ifHaveBean("mockUserConfigApply"));
        configMap.put("enableSystemDictProtect", dictProperties.getEnableSystemDictProtect());
        return configMap;
    }


    private void updatePass(HttpServletRequest request, TaUserVo vo, String oldPassword, String newPassword) {
        // 判断是否为登录页面修改密码,是则校验验证码
        if (captchaProperties.isUserCheckCode() && request != null) {
            if (StringUtils.equals(captchaProperties.getCaptchaType(), CaptchaTypeEnum.SIMPLE.getCodeValue())) {
                captchaService.verification(request);
            } else {
                boolean flag = captchaService.verification(request).isSuccess();
                if (!flag) {
                    throw new AppException("验证码错误");
                }
            }
        }
        if (ValidateUtil.isEmpty(oldPassword)) {
            throw new AppException("原密码为空");
        }
        if (null == vo || ValidateUtil.isEmpty(vo.getUserId())) {
            throw new AppException("账户或密码错误！");
        }
        if (StringUtils.equals(cryptoService.decryptWithAsymmetric(newPassword),cryptoService.decryptWithAsymmetric(oldPassword))){
            throw new AppException("新密码不能与旧密码相同");
        }
        try {
            //校验旧密码
            if (!encoder.matches(oldPassword, vo.getPassword())) {
                //判断账号是否已锁定，以免无限增加错误次数
                if (taUserAutoUnlockService.isUserLock(vo.getUserId())) {
                    throw new AppException("账号已锁定，请联系管理员解锁");
                }

                //账号判定错误次数自动锁定处理
                Integer errorNum = ValidateUtil.isEmpty(vo.getPasswordDefaultNum()) ? 0 : vo.getPasswordDefaultNum();
                taUserAutoUnlockService.lockUser(new TaLockUser(vo.getUserId(), errorNum));

                //判断账号是否已锁定，提示用户
                if (taUserAutoUnlockService.isUserLock(vo.getUserId())) {
                    throw new AppException("原密码多次错误，账号已锁定");
                }

                //本次密码错误，但账号未锁定
                throw new AppException("账户或密码错误！");
            }

            //校验新密码强度,防止接口攻击
            //update by wanggan for 5.2.x,由于加密是从前端的公钥进行加密,所以后端的解密,也应当是后端对应的解密才行
            String plainPwd = cryptoService.decryptWithAsymmetric(newPassword);
            if (!encryptUserService.checkPasswordIsFit(plainPwd)) {
                throw new AppException("新密码强度不足，修改失败");
            }
        } catch (AppException e) {
            //发送操作日志事件-密码修改-不被允许
            orgOpLogEventPublish.pubUserOpLogEvent(OP_TYPE_MODIFY_PWD, vo.getUserId(), "oldPassword:" + oldPassword + "\n->\nnewPassword:" + newPassword, CommonConstants.NO);
            throw e;
        }

        //更新密码
        userManagementWriteService.updateUserPwdByUserId(vo.getUserId(), newPassword);
        //发送操作日志事件-密码修改
        orgOpLogEventPublish.pubUserOpLogEvent(OP_TYPE_MODIFY_PWD, vo.getUserId(), "oldPassword:" + oldPassword + "\n->\nnewPassword:" + newPassword, CommonConstants.YES);
    }

    @Override
    public List<TaUserVo> queryUsersByMobile(String mobile) {
        return userManagementReadService.queryUserVoByMobile(mobile);
    }


    @Override
    public boolean ifHaveBean(String beanName) {
        try {
            applicationContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
        return true;
    }
}
