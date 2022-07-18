package com.kou.gulimall.member.feign;

import com.kou.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("coupon-service")
public interface  CouponFeignService {


    @PostMapping("/coupon/coupon/member/list")
    R memberCoupons();

}
