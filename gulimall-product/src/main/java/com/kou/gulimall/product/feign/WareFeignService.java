package com.kou.gulimall.product.feign;

import com.kou.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("ware-service")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    public R queryHasStock(@RequestBody List<Long> skuIdList);
}
