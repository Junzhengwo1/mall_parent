package com.kou.gulimall.product.controller.web;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.service.SkuInfoService;
import com.kou.gulimall.product.vo.SkuItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Api(value = "Item",tags = "Item页面")
@RestController
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;


    @ApiOperation("ITEM页面 ")
    @GetMapping({"Item/{skuId}.html"})
    public R skuItem(@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = skuInfoService.item(skuId);

        return R.ok().put("data",vo); //模拟的item 页面项目中没有
    }
}
