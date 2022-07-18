package com.kou.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkusVo {

    private List<AttrForSpuVo> attrForSpuVos;

    private String skuName;

    private BigDecimal peice;

    private String skuTitle;

    private String skuSubtitle;

    private List<ImagesVo> imagesVos;

    private List<String> descar;

    private BigDecimal fullCount;

    private BigDecimal discount;

    private int countStatus;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private int priceStatus;

    private List<MemberPriceVo> memberPriceVos;


}
