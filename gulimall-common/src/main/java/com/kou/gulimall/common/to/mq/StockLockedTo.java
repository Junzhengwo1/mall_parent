package com.kou.gulimall.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {

    //库存工作单id
    private Long id;

    //工作单详情的Ids
    private StockDetailTo stockDetailTo;
}
