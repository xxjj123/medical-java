package com.yinhai.ta404.index;

import com.yinhai.ta404.component.core.examine.annotation.TaSsoHandler;
import com.yinhai.ta404.component.security.base.autoconfigure.WebSecurityConfig;
import com.yinhai.ta404.core.constants.TaHttpStatusConstants;
import com.yinhai.ta404.core.event.EventPublish;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.restservice.BaseRestService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import com.yinhai.ta404.core.restservice.resultbean.ResultBean;
import com.yinhai.ta404.core.security.vo.UserAccountVo;
import com.yinhai.ta404.core.service.time.TimeService;
import com.yinhai.ta404.core.utils.*;
import com.yinhai.ta404.service.IndexService;
import com.yinhai.ta404.websecurity.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static com.yinhai.ta404.core.CommonConstants.*;


/**
 * 主页 rest service
 *
 * @author zhongzb [zhongzb@yinhai.com]
 * @author MinusZero [hesh@yinhai.com]
 * @since 5.0
 */
@RestService("indexRestService")
public class IndexRestService extends BaseRestService {

    @Resource
    private IndexService indexService;

    @Resource
    private EventPublish eventPublish;

    @Resource
    private WebSecurityConfig securityConfig;

    @Autowired
    private CryptoService cryptoService;

    @Resource
    TimeService timeService;

    @GetMapping("healthCheck")
    public void healthCheck() {
        setSuccess(true);
    }

    /**
     * 默认打开页面请求 用于判断是否登录
     */
    @PostMapping("defaultOpen")
    public void defaultOpen(HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
        UserAccountVo userAccountVo = getCurUserAccount();
        if (null == userAccountVo || ValidateUtil.isEmpty(userAccountVo.getUserId())) {
            // 判断是否开启单点登录
            if (securityConfig.isOpenSso()) {
                TaSsoHandler taSsoHandler = ServiceLocator.getService(TaSsoHandler.class);
                taSsoHandler.ssoLoginHandle(servletRequest, response);
                return;
            }
            ResultBean result = result();
            result.setSuccess(false);
            result.addError(TaHttpStatusConstants.AUTHENTICATION_ERROR_CODE, "未登录");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonFactory.bean2json(result));
            return;
        }
        ResultBean result = result();
        result.setSuccess(true);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonFactory.bean2json(result));
    }

    @PostMapping("getCurUserAccount")
    public void getUserInfo() {
        UserAccountVo userAccountVo = getCurUserAccount();
        if (null == userAccountVo || ValidateUtil.isEmpty(userAccountVo.getUserId())) {
            ResultBean result = result();
            result.setSuccess(false);
            result.addError(TaHttpStatusConstants.AUTHENTICATION_ERROR_CODE, "未登录");
            return;
        }
        setData("curUserAccount", indexService.getUserInfo(userAccountVo));
//        setData("passwordRSA", indexService.isPassRSA());
        setData("passwordLevel", indexService.getPasswordLevel());
        setData("encryptLoginId", indexService.isEncryptLoginId());
        setData("isSSO", securityConfig.isOpenSso());
    }

    /**
     * 获取配置
     */
    @PostMapping("getCryptoInfo")
    public void getCryptoInfo() {
        /**
         * 新增加密信息
         */
        setData("cryptoInfo", cryptoService.getCryptoInfo());
    }

    /**
     * 将随机密码发送到后端
     *
     * @param key 是先将密码进行base64,然后使用非对称加密传输过来的数据
     */
    @PostMapping("getToken")
    public void setKey(String key) {
        /**
         * 先进行非对称解密,然后将base64的数据生成token
         */
        if (StringUtils.isNullOrEmpty(key)) {
            throw new AppException("无效key");
        }
        String serviceKey = cryptoService.decryptWithAsymmetric(key);
        String token = cryptoService.generateToken(serviceKey);
        if (StringUtils.isNullOrEmpty(token)) {
            throw new AppException("无效key");
        }
        setData("token", token);
    }

    @PostMapping("getSysInfo")
    public void getSysInfo() {
        setData("openSocialLogin", indexService.ifHaveBean("socialConfigApply"));
        setData("openSmsLogin", indexService.ifHaveBean("smsConfigApply"));
        setData("openMockUser", indexService.ifHaveBean("mockUserConfigApply"));
    }

    @PostMapping("changePassword")
    public void changePassword(String oldPassword, String newPassword, String userId) throws IOException {
        HttpServletRequest request = getRequest();
//        构造事件信息
        Map<String, Object> eventDto = getEventDto(userId, request);
        //修改密码
        try {
            indexService.changePassByUserId(oldPassword, newPassword, userId);
        } catch (AppException e) {
            setError(e.getMessage());
            eventPublish.publish(eventDto, MODIFY_PWD_FAIL_EVENT_ID);

            return;
        }
        setData("message", "修改密码成功");
        eventPublish.publish(eventDto, MODIFY_PWD_EVENT_ID);
    }

    /**
     * 构造事件信息
     *
     * @param userId  账户Id
     * @param request HttpRequest
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */
    private Map<String, Object> getEventDto(String userId, HttpServletRequest request) throws UnknownHostException {
        Map<String, Object> eventDto = new HashMap<>(16);
        eventDto.put(EVENT_PARAM_SESSION, null);
        eventDto.put(EVENT_PARAM_SESSIONID, UUIDUtils.getUUID());
        eventDto.put(EVENT_PARAM_USERID, userId);
        eventDto.put(EVENT_PARAM_CLIENTIP, WebUtil.getClientIp(request));
        eventDto.put(EVENT_PARAM_SERVERIP, NetUtils.getLocalHost());
        eventDto.put(EVENT_PARAM_SESSIONTIME, timeService.getSysTimestamp());
        eventDto.put(EVENT_PARAM_CLIENTSYSTEM, request.getParameter(REQUEST_PARAM_CLIENTSYSTEM));
        eventDto.put(EVENT_PARAM_CLIENTBROWSER, request.getParameter(REQUEST_PARAM_CLIENTBROWSER));
        eventDto.put(EVENT_PARAM_CLIENTSCREENSIZE, request.getParameter(REQUESTPARAM_CLIENTSCREENSIZE));
        return eventDto;
    }
}
