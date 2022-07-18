package com.kou.gulimall.product.feign;


import com.kou.gulimall.common.to.SkuReductionTo;
import com.kou.gulimall.common.to.SpuBoundsTo;
import com.kou.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("coupon-service")
public interface CouponFeignService {


    /**
     * todo
     * 远程调用的逻辑CouponFeignService.saveSpuBounds(spuBoundsTo)
     * 1、将这个对象转为json
     * 2、去注册中心中找到 coupon-service这个服务 并发送请求/coupon/spubounds/save
     *      会将上一步转的json数据放在请求体位置
     * 3、对方服务收到的请求，请求体里边的json数据
     * @param spuBoundsTo 传输对象
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
