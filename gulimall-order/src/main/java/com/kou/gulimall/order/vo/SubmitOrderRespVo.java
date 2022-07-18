package com.kou.gulimall.order.vo;

import com.kou.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderRespVo {

    private OrderEntity orderEntity;

    private Integer statusCode;
}
