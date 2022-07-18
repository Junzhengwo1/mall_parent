package com.kou.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberPriceVo {

    private Long id;

    private String name;

    private BigDecimal price;

}
