package com.kou.gulimall.authserver.feign;

import com.kou.gulimall.authserver.vo.UserLoginVo;
import com.kou.gulimall.authserver.vo.UserRegistVo;
import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("member-service")
public interface MemberFeignService {

    /**
     * 注册功能
     */
    @ApiOperation("会员注册")
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    /**
     * 登录功能
     */
    @ApiOperation("会员登录")
    @PostMapping("/member/member/Login")
    R Login(@RequestBody UserLoginVo vo);



}
