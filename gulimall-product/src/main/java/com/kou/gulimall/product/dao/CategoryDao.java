package com.kou.gulimall.product.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kou.gulimall.product.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
