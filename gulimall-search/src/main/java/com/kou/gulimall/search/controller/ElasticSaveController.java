package com.kou.gulimall.search.controller;

import com.kou.gulimall.common.exception.BizCodeEnum;
import com.kou.gulimall.common.to.SkuEsModel;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.search.service.ProductSaveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@Api(value = "商品检索",tags = "商品检索")
@RestController
@RequestMapping("/search")
public class ElasticSaveController {



    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     */
    @ApiOperation("商品上架")
    @PostMapping("/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){

        Boolean b = false;
        try {
            b= productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXPCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXPCEPTION.getMsg());
        }
        if (!b) {
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXPCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXPCEPTION.getMsg());
        }

    }

}
