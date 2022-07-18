package com.kou.gulimall.order.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.kou.gulimall.common.constant.AuthServerConstant;
import com.kou.gulimall.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * todo 拦截当前登录用户信息‘也拦截 如果不是登录的话 其他接口也是访问不了的
 *
 */
@Component
public class LoginUserInterCeptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/orderStatus/**", requestURI);
        if (match){
            return true;
        }

        // todo  session
        MemberRespVo loginUserinfo = (MemberRespVo) request.getSession(true).getAttribute(AuthServerConstant.LOGIN_USER);
        if(ObjectUtil.isNotNull(loginUserinfo)){
            loginUser.set(loginUserinfo);
            return true;
        }else {
            //没登陆就去登录
            return false;
        }


    }
}
