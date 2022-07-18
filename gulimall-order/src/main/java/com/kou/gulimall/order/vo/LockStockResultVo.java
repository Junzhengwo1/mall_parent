package com.kou.gulimall.order.vo;

import lombok.Data;

@Data
public class LockStockResultVo {

    private Long skuId;

    private Integer num;

    private Boolean locked;


}
