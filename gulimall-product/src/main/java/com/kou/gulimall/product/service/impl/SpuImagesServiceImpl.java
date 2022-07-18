package com.kou.gulimall.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.kou.gulimall.common.utils.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.product.dao.SpuImagesDao;
import com.kou.gulimall.product.entity.SkuImagesEntity;
import com.kou.gulimall.product.entity.SpuImagesEntity;
import com.kou.gulimall.product.service.SpuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void saveImages(Long id, List<String> images) {
        if(CollectionUtil.isEmpty(images)){
            List<SpuImagesEntity> spuImagesEntities = images.stream().map(o -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(id);
                spuImagesEntity.setImgUrl(o);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            this.saveBatch(spuImagesEntities);
        }

    }



}