package com.kou.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kou.gulimall.common.utils.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.product.dao.CategoryBrandRelationDao;
import com.kou.gulimall.product.entity.BrandEntity;
import com.kou.gulimall.product.entity.CategoryBrandRelationEntity;
import com.kou.gulimall.product.service.BrandService;
import com.kou.gulimall.product.service.CategoryBrandRelationService;
import com.kou.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private CategoryBrandRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //拿到对应的名字
        categoryBrandRelation.setBrandName(brandService.getById(brandId).getName());
        categoryBrandRelation.setCatelogName(categoryService.getById(catelogId).getName());
        this.save(categoryBrandRelation);
    }

    @Transactional
    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        LambdaUpdateWrapper<CategoryBrandRelationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CategoryBrandRelationEntity::getBrandId,brandId);
        this.update(relationEntity,updateWrapper);

    }

    @Transactional
    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getbrandsBycatId(Long catId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity ::getCatelogId,catId);
        List<CategoryBrandRelationEntity> brandRelationEntities = categoryBrandRelationService.list(wrapper);
        List<BrandEntity> brandEntities = brandRelationEntities.stream().map(o -> {
            BrandEntity brandEntity = brandService.getById(o.getBrandId());
            return brandEntity;
        }).collect(Collectors.toList());
        return brandEntities;
    }

}