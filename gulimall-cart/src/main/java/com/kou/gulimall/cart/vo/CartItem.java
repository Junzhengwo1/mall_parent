package com.kou.gulimall.cart.vo;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项内容
 */
@Data
public class CartItem implements Serializable {

    private Long skuId;
    
    private String title;
    
    private Boolean check = true;
    
    private String imge;
    
    private List<String> skuAttr;
    
    private BigDecimal price;
    
    private Integer count;
    
    private BigDecimal totalPrice; //总价


    /**
     * 计算总价
     * @return
     */
    public BigDecimal calTotalPrice() {
       return this.price.multiply(new BigDecimal(""+this.count));
    }
    
    

    
}
