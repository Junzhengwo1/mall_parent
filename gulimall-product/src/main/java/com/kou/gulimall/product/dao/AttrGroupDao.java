package com.kou.gulimall.product.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kou.gulimall.product.entity.AttrGroupEntity;
import com.kou.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catelogId") Long catalogId);
}
