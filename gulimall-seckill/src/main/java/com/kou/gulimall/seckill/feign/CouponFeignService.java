package com.kou.gulimall.seckill.feign;

import com.kou.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("coupon-service")
public interface CouponFeignService {


    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();

}
