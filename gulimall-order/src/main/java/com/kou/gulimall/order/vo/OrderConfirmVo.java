package com.kou.gulimall.order.vo;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 订单确认页需要用到的数据
 */
@Data
public class OrderConfirmVo {

    //收获地址。ums_member_recevice_address表
    private List<MemberAddressVo> memberAddressVos;

    //所有选中的购物项
    private List<OrderItemVo> orderItemVos;

    //优惠卷信息
    private Integer integeration;

    //总价
    private BigDecimal total;

    //实际支付
    private BigDecimal payPrice;

    //放重令牌
    private String orderToken;

    //商品数量
    private Integer num;

    //是否库存
    private Map<Long,Boolean> stockMap;

    public Integer calNum(){
        int n = 0;
        if(CollectionUtil.isNotEmpty(orderItemVos)) {
            for (OrderItemVo itemVo : orderItemVos) {
                n += itemVo.getCount();
            }
        }
        return n;
    }

    public BigDecimal calTotal(){
        BigDecimal sum = new BigDecimal("0.00");
        if(CollectionUtil.isNotEmpty(orderItemVos)){
            for (OrderItemVo itemVo : orderItemVos) {
                sum=sum.add(itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount().toString())));
            }
        }
        return sum;
    }


    public BigDecimal calPayPrice(){
        return calTotal();
    }
}
