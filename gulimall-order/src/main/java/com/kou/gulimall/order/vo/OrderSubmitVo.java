package com.kou.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * 订单提交vo
 */
@Data
public class OrderSubmitVo {

    //地址
    private Long addrId;

    //支付方式
    private Integer payType;

    //无需提交购买的商品，只需要再去购物车查商品就行

    //orderToken
    private String orderToken;

    //应付总额(验价)
    private BigDecimal payPrice;

    //用户相关信息 在session中取

    //订单备注
    private String conent;




}
