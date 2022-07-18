package com.kou.gulimall.cart.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.kou.gulimall.cart.vo.UserInfoTo;
import com.kou.gulimall.common.constant.AuthServerConstant;
import com.kou.gulimall.common.constant.CartConstant;
import com.kou.gulimall.common.vo.MemberRespVo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;

/**
 * 在执行目标方法之前，判断用户的登录状态，并封装传递给controller
 */
public class CartIntercepter implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> toThreadLocal=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        //todo 从session中获取用户信息
        MemberRespVo attribute = (MemberRespVo) request.getSession(true).getAttribute(AuthServerConstant.LOGIN_USER);
        if(ObjectUtil.isNotNull(attribute)){
            //已经登录的情况
            userInfoTo.setUserId(attribute.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length!=0 && CollectionUtil.isNotEmpty(Arrays.asList(cookies))){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if(CartConstant.TEMP_USER_COOKIE_NAME.equals(name)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //如果没有临时用户
        if(StrUtil.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        toThreadLocal.set(userInfoTo);
        return true;
    }


    /**
     * 业务执行之后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = toThreadLocal.get();
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("localhost");//模拟的域名，这里就用本地测试用 使用的swagger来测试
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }



}
