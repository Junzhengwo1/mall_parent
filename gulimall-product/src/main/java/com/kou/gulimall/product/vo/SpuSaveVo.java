package com.kou.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * 使用JSR303校验
 */
@Data
public class SpuSaveVo {

    private String spuName;

    private String spuDescription;

    private Long catalogId; //数据库中字母写错了

    private Long brandId;

    private BigDecimal weight;

    private int publishStatus;

    private List<String> decript;

    private List<String> images;

    private BoundsVo boundsVo;

    private List<BaseAttrsVo> baseAttrsVos;

    private List<SkusVo> skusVos;

}
