package com.kou.gulimall.cart.vo;

import cn.hutool.core.collection.CollectionUtil;

import java.math.BigDecimal;
import java.util.List;


/**
 * 购物车VO
 */
@SuppressWarnings("all")
public class CartVo {


    private List<CartItem> cartItems;

    private Integer countNum; //商品数量 种类 例如每一种商品 有多少件

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");


    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Integer getCountNum() {
        int count = 0;
        if(CollectionUtil.isNotEmpty(cartItems)){
            for (CartItem cartItem : cartItems) {
                count+=cartItem.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        int count = 0;
        if(CollectionUtil.isNotEmpty(cartItems)){
            for (CartItem cartItem : cartItems) {
                count+=1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0.00");
        if(CollectionUtil.isNotEmpty(cartItems)){
            for (CartItem cartItem : cartItems) {
                if(cartItem.getCheck()){
                    BigDecimal totalPrice = cartItem.calTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        BigDecimal result = amount.subtract(getReduce());
        return result;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
