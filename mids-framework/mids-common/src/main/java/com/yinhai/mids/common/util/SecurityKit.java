package com.yinhai.mids.common.util;

import com.yinhai.ta404.core.security.vo.UserAccountVo;
import com.yinhai.ta404.core.utils.WebUtil;

import javax.annotation.Nullable;

/**
 * 用户登录信息工具类
 *
 * @author zhuhs
 * @date 2022/09/30 15:17
 */
public class SecurityKit {

    /**
     * 返回当前登录用户的userId，如果获取不到用户，会返回null
     *
     * @return {@link String }
     * @author zhuhs 2022/09/30 15:21
     */
    @Nullable
    public static String currentUserId() {
        UserAccountVo curUserAccountVo = WebUtil.getCurUserAccountVo();
        return curUserAccountVo != null ? curUserAccountVo.getUserId() : null;
    }
}
