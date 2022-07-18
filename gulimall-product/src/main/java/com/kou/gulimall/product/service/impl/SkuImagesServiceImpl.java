package com.kou.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kou.gulimall.common.utils.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.product.dao.SkuImagesDao;
import com.kou.gulimall.product.entity.SkuImagesEntity;
import com.kou.gulimall.product.service.SkuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;



@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatchSkuImages(List<SkuImagesEntity> skuImagesEntities) {
        this.saveBatch(skuImagesEntities);
    }

    @Override
    public List<SkuImagesEntity> getImagesBySkuId(Long skuId) {
        return this.list(new LambdaQueryWrapper<SkuImagesEntity>().eq(SkuImagesEntity::getSkuId,skuId));
    }

}