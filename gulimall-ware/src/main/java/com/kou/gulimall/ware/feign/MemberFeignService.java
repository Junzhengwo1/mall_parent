package com.kou.gulimall.ware.feign;

import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("member-service")
public interface MemberFeignService {

    @ApiOperation("收获地址的信息")
    @RequestMapping("member/memberreceiveaddress/info/{id}")
    public R addrInfo(@PathVariable("id") Long id);

}
