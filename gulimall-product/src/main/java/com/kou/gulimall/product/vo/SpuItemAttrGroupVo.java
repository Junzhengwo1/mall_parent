package com.kou.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<SpuBaseAttrVo> attrValues;
}
