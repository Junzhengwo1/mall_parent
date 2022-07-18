package com.kou.ssoserver.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloTestController {

    /**
     * 无需登录就可以访问
     *
     */
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }


    /**
     * 需要登后才可访问
     * @return
     */
    @GetMapping("/login")
    public String emo(){
        return "eml";
    }


}
