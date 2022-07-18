package com.kou.gulimall.order.feign;

import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("product-service")
public interface ProductFeignService {

    @ApiOperation(value = "Spu信息forSkuId")
    @GetMapping("product/spuinfo/bySkuId/{skuId}")
    public R SpuInfoForSkuId(@PathVariable("skuId") Long skuId );
}
