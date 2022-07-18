package com.kou.gulimall.thirdparty.controller;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.thirdparty.componment.SmsComponment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
//@RequestMapping("/sms")
public class SmsSendController {


    @Autowired
    private SmsComponment smsComponment;

    /**
     * 提供给别的服务调用的
     * @param code 验证码
     * @param phone 电话
     * @return 结果信息
     */
    @GetMapping("/sms/send-sms-code")
    public R sendSmsCode(@RequestParam("code") String code,@RequestParam("phone") String phone){
        String s = smsComponment.sendSmsCode(code, phone);
        log.info("验证码发送的结果{}",s);
        return R.ok();
    }
}
