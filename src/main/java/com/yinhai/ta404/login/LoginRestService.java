package com.yinhai.ta404.login;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.yinhai.ta404.core.service.time.TimeService;
import com.yinhai.ta404.core.utils.NetUtils;
import com.yinhai.ta404.core.utils.UUIDUtils;
import com.yinhai.ta404.domain.core.orguserauth.interfaces.vo.TaUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.yinhai.ta404.component.security.base.service.EncryptUserService;
import com.yinhai.ta404.core.event.EventPublish;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.restservice.BaseRestService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import com.yinhai.ta404.core.utils.ValidateUtil;
import com.yinhai.ta404.core.utils.WebUtil;
import com.yinhai.ta404.service.IndexService;

import static com.yinhai.ta404.core.CommonConstants.*;


/**
 * 登录 rest service
 *
 * @author zhongzb [zhongzb@yinhai.com]
 * @author MinusZero [hesh@yinhai.com]
 * @author wanggan
 * @since 5.0
 */
@RestService("loginRestService")
public class LoginRestService extends BaseRestService {
    @Resource
    private IndexService indexService;

    @Resource
    private EventPublish eventPublish;

    @Resource
    private EncryptUserService encryptLoginIdService;

    @Resource
    TimeService timeService;

    /**
     * 检查账户是否存在
     *
     * @param username loginId
     * @param request  request
     */
    @PostMapping("checkUser")
    public void checkUser(String username, HttpServletRequest request) {
        username = encryptLoginIdService.getLoginId(username);
        int validationErrorNumber = indexService.getPassErrorNum(username);
        setData("sessionPasswordErrorNumber", validationErrorNumber);
//        request.getSession(true).setAttribute(PW_VALIDATION_ERROR_NUMBER + username, validationErrorNumber);
    }

    /**
     * 获取配置
     *
     * @param request request
     */
    @GetMapping("getConfig")
    public void getConfig(HttpServletRequest request) {
        setData("configMap", indexService.getConfig(request));
    }

    /**
     * 修改密码
     *
     * @param request     request
     * @param loginId     账号
     * @param newPassword 新密码
     * @param oldPassword 旧密码
     */
    @PostMapping("changePassword")
    public void changePassword(String loginId, HttpServletRequest request, String oldPassword, String newPassword) throws IOException {
        loginId = encryptLoginIdService.getLoginId(loginId);

//        构造事件信息
        Map<String, Object> eventDto = getEventDto(loginId, request);
        //修改密码
        try {
            indexService.changePassByLoginId(request, loginId, oldPassword, newPassword);
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
     * @param loginId 登录Id
     * @param request HttpServletRequest
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */
    private Map<String, Object> getEventDto(String loginId, HttpServletRequest request) throws UnknownHostException {
        Map<String, Object> eventDto = new HashMap<>(16);
        eventDto.put(EVENT_PARAM_SESSION, null);
        eventDto.put(EVENT_PARAM_SESSIONID, UUIDUtils.getUUID());
        eventDto.put(EVENT_PARAM_LOGINID, loginId);
        eventDto.put(EVENT_PARAM_CLIENTIP, WebUtil.getClientIp(request));
        eventDto.put(EVENT_PARAM_SERVERIP, NetUtils.getLocalHost());
        eventDto.put(EVENT_PARAM_SESSIONTIME, timeService.getSysTimestamp());
        eventDto.put(EVENT_PARAM_CLIENTSYSTEM, request.getParameter(REQUEST_PARAM_CLIENTSYSTEM));
        eventDto.put(EVENT_PARAM_CLIENTBROWSER, request.getParameter(REQUEST_PARAM_CLIENTBROWSER));
        eventDto.put(EVENT_PARAM_CLIENTSCREENSIZE, request.getParameter(REQUESTPARAM_CLIENTSCREENSIZE));
        return eventDto;
    }

    /**
     * 检查 电话号码
     *
     * @param mobile 电话号码
     */
    @PostMapping("checkMobile")
    public void checkMobile(String mobile) {
        mobile = encryptLoginIdService.getLoginId(mobile);
        List<TaUserVo> userVos = indexService.queryUsersByMobile(mobile);
        if (ValidateUtil.isEmpty(userVos)) {
            setError("此号码未绑定账户，请检查号码");
            return;
        }
        List<Map<?,?>> loginIds = new ArrayList<>();
        for (TaUserVo userVo : userVos) {
            Map<String, String> loginIdMapLoginId = new HashMap<>(4);
            loginIdMapLoginId.put("title", userVo.getLoginId());
            loginIdMapLoginId.put("value", userVo.getLoginId());
            loginIds.add(loginIdMapLoginId);
        }
        setData("loginIds", loginIds);
        if (userVos.size() == 1) {
            setData("justOne", true);
        }
    }
}
