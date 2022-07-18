package com.kou.gulimall.order.to;

import com.kou.gulimall.order.entity.OrderEntity;
import com.kou.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderGreateTo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> itemEntities;

    //应付价格
    private BigDecimal payPrice;

    //运费
    private BigDecimal fare;

}
