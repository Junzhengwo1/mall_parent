package com.kou.gulimall.product.controller.web;


import com.kou.gulimall.common.constant.AuthServerConstant;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.common.vo.MemberRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "session测试")
@RestController
@EnableRedisHttpSession
public class SessionTestController {

    @ApiOperation("session获取当前登录用户信息")
    @GetMapping("/getCurrentLoginUser")
    public R getCurrentLoginUser(HttpServletRequest request){
        MemberRespVo loginUser = (MemberRespVo) request.getSession(true).getAttribute(AuthServerConstant.LOGIN_USER);
        return R.ok().put(AuthServerConstant.LOGIN_USER,loginUser);
    }
}
