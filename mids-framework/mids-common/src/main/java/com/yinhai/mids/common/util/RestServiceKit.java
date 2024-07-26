package com.yinhai.mids.common.util;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.ta404.core.restservice.BaseRestService;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import com.yinhai.ta404.core.restservice.resultbean.ResultBean;
import com.yinhai.ta404.core.security.vo.UserAccountVo;
import com.yinhai.ta404.core.utils.JsonFactory;
import com.yinhai.ta404.core.utils.ServiceLocator;
import com.yinhai.ta404.core.utils.WebUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

/**
 * 将{@link BaseRestService}的方法提取形成的工具类，以处理不方便继承的情况
 *
 * @author zhuhs
 * @date 2022/09/07 18:11
 */
public class RestServiceKit {

    private static final Log log = LogFactory.get();

    private static final String RESULT = "result";
    private static final String PAGE_BEAN = "pageBean";

    public static ResultBean result() {
        ResultBean resultBean = (ResultBean) getRequest().getAttribute("_WEB_RESULT_BEAN");
        if (null == resultBean) {
            resultBean = (ResultBean) ServiceLocator.getAppContext().getBean(ResultBean.class);
            getRequest().setAttribute("_WEB_RESULT_BEAN", resultBean);
        }

        return resultBean;
    }

    public static void setData(String fieldId, Object value) {
        ResultBean resultBean = result();
        resultBean.addField(fieldId, value);
    }

    public static void setData(Map<String, Object> data, boolean clear) {
        ResultBean resultBean = result();
        resultBean.setData(data, clear);
    }

    public static void setError(String errorMsg) {
        setSuccess(false);
        setError("418", errorMsg);
    }

    public static void setError(String errorId, String errorMsg) {
        setSuccess(false);
        setObjErrors(errorId, errorMsg);
    }

    public static void setErrors(String[] errorMsgs) {
        setSuccess(false);
        setErrors("418", errorMsgs);
    }

    public static void setErrors(String errorId, String[] errorMsgs) {
        setObjErrors(errorId, errorMsgs);
    }

    public static void setObjErrors(String errorId, Object errorMsgs) {
        ResultBean resultBean = result();
        if (null != errorId && null != errorMsgs) {
            Map<String, String[]> parameterMap = getRequest().getParameterMap();
            if (errorMsgs instanceof String[]) {
                String[] var5 = (String[]) ((String[]) errorMsgs);
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String errorMsg = var5[var7];
                    resultBean.addError(errorId, JsonFactory.bean2json(parameterMap), errorMsg);
                }
            } else {
                resultBean.addError(errorId, JsonFactory.bean2json(parameterMap), (String) errorMsgs);
            }
        }
    }

    public static void setValidError(String errorCode, String parameter, String errorMsg) {
        ResultBean resultBean = result();
        resultBean.addError(errorCode, parameter, errorMsg);
    }

    public static void setPageBean(Page<?> page) {
        result().addField("pageBean", page);
    }

    public static void setSuccess(boolean success) {
        result().setSuccess(success);
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public static MultipartHttpServletRequest getMultipartHttpServletRequest(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        return multipartResolver.isMultipart(request) ? (MultipartHttpServletRequest) request : null;
    }

    public static void writeJsonToClient(Object obj) {
        throw new UnsupportedOperationException();
    }

    public static String getCurUserId() {
        UserAccountVo userAccountVo = WebUtil.getCurUserAccountVo(getRequest());
        return Objects.isNull(userAccountVo) ? null : userAccountVo.getUserId();
    }

    public static UserAccountVo getCurUserAccount() {
        UserAccountVo userAccountVo = WebUtil.getCurUserAccountVo(getRequest());
        return Objects.isNull(userAccountVo) ? new UserAccountVo() : userAccountVo;
    }
}
