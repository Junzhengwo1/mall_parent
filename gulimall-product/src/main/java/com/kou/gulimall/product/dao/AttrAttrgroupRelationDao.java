package com.kou.gulimall.product.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kou.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("res") List<AttrAttrgroupRelationEntity> relationEntities);
}
