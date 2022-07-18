package com.kou.gulimall.cart.feign;

import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("product-service")
public interface ProductFeignService {

    /**
     * 信息
     */
    @ApiOperation("sku详情信息")
    @GetMapping("product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);


    @ApiOperation("销售属性列表")
    @GetMapping("product/skusaleattrvalue/stringList{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @ApiOperation("查询商品的最新价格")
    @GetMapping("product/skuinfo/getRealTimePrice/{skuId}")
    public BigDecimal getRealTimePrice(@PathVariable("skuId") Long skuId);
}
