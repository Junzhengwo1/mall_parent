package com.kou.gulimall.seckill.controller;

import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "暴露定时上架controller")
public class OutController {

    @ApiOperation("定时上架")
    @GetMapping("/seckill/upload")
    public R uploadProduct(){

        return R.ok();
    }
}
