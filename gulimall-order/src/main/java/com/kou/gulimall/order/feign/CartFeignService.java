package com.kou.gulimall.order.feign;


import com.kou.gulimall.order.vo.OrderItemVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("cart-service")
public interface CartFeignService {


    @ApiOperation("获取当前用户的购物项")
    @GetMapping("cart/currentUserCartItems")
    public List<OrderItemVo> getCurrentUserCartItems();

}
