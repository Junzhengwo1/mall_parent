package com.kou.gulimall.coupon;

import com.kou.gulimall.coupon.service.HomeAdvService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Autowired
    private HomeAdvService homeAdvService;

    @Test
    void contextLoads() {

        homeAdvService.list().forEach(System.out ::println);
    }

}
