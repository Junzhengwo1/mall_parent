package com.kou.gulimall.ware.feign;

import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("order-service")
public interface OrderFeignService {

    @ApiOperation("订单状态")
    @GetMapping("order/order/orderStatus/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
