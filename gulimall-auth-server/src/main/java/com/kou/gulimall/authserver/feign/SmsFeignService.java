package com.kou.gulimall.authserver.feign;

import com.kou.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("third-service")
public interface SmsFeignService {

    @GetMapping("/sms/send-sms-code")
    R sendSmsCode(@RequestParam("code") String code, @RequestParam("phone") String phone);
}
