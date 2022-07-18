package com.kou.gulimall.product.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kou.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.kou.gulimall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
