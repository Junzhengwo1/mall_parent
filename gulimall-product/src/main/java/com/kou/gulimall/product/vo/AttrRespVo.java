package com.kou.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {

    private String catelogName;
    private String groupName;
    /**
     *找到商品的完整路径
     */
    private Long[] catelogPath;
}
