package com.kou.gulimall.order.feign;

import com.kou.gulimall.order.vo.MemberAddressVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("member-service")
public interface MemberFeignService {

    @ApiOperation("获取地址列表")
    @GetMapping("member/memberreceiveaddress/{memberId}/address")
    public List<MemberAddressVo> getAddress(@PathVariable Long memberId);

}
