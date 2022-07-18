package com.kou.gulimall.order.feign;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.order.vo.WareSkuLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("ware-service")
public interface WareFeginService {

    @ApiOperation("查询是否有库存")
    @PostMapping("ware/waresku/hasStock")
    public R queryHasStock(@RequestBody List<Long> skuIdList);

    @ApiOperation("获取运费信息")
    @GetMapping("ware/wareinfo/getFare")
    public R getFare(@RequestParam("addrId") Long addrId);

    @ApiOperation("订单锁定库存")
    @PostMapping("ware/waresku/lock/order")
    public R lockOrder(@RequestBody WareSkuLockVo vo);

}
