package com.kou.gulimall.product.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.product.dao.SkuInfoDao;
import com.kou.gulimall.product.entity.SkuImagesEntity;
import com.kou.gulimall.product.entity.SkuInfoEntity;
import com.kou.gulimall.product.entity.SpuInfoDescEntity;
import com.kou.gulimall.product.service.*;
import com.kou.gulimall.product.vo.SkuItemSaleAttrVo;
import com.kou.gulimall.product.vo.SkuItemVo;
import com.kou.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = Wrappers.lambdaQuery();
        String key = (String) params.get("key");
        if(StrUtil.isNotBlank(key)){
            queryWrapper.and(o->o.eq(SkuInfoEntity::getSkuId,key).or().like(SkuInfoEntity::getSkuName,key));
        }
        String brandId = (String) params.get("brandId");
        Number numBrandId = NumberUtil.parseNumber(brandId);
        if(StrUtil.isNotBlank(brandId) && numBrandId.intValue()!=BigDecimal.ZERO.intValue()){
            queryWrapper.eq(SkuInfoEntity::getBrandId,brandId);
        }
        String catelogId = (String) params.get("catelogId");
        Number numCatelogId = NumberUtil.parseNumber(catelogId);
        if(StrUtil.isNotBlank(catelogId) && numCatelogId.intValue() != BigDecimal.ZERO.intValue()){
            queryWrapper.eq(SkuInfoEntity::getCatalogId,catelogId);
        }
        String min = (String) params.get("min");
        if(StrUtil.isNotBlank(min)){
            queryWrapper.ge(SkuInfoEntity::getPrice,min);
        }
        String max = (String) params.get("max");
        Number number = NumberUtil.parseNumber(max);
        if(StrUtil.isNotBlank(max) && number.doubleValue()>0){
            queryWrapper.le(SkuInfoEntity::getPrice,max);
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }



    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper= Wrappers.lambdaQuery();
        queryWrapper.eq(SkuInfoEntity::getSpuId,spuId);
        List<SkuInfoEntity> list = this.list(queryWrapper);
        return list;
    }

    /**
     * TODO　异步相关回顾
     *      注意点：
     *      我们说的runable 接口没有返回值，
     *      当然这个返回值是可以通过
     *      FutureTask(Runnable runnable,v result) 去把对应的值赋给改对象
     *线程池的 execute()与commit（）的区别 就是是否能获得返回值
     *      线程池就是能控制资源，保证系统稳定
     *
     *
     */

    /**
     * //todo 异步场景使用 重点回顾
     * 异步编排 优化查询顺序
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku 的基本信息
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        },executor);

        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync((res) -> {
            Long spuId = res.getSpuId();
            //3、获取spu的销售属性的全部组合 todo 重点功能 值得反复回味
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> spuInfoFuture = infoFuture.thenAcceptAsync(res -> {
            //4、获取spu介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> skuAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //5、获取sku规格参数
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            //2、 sku 的图片信息
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntities);
        }, executor);

        //等待所有任务完成
        CompletableFuture.allOf(attrFuture, spuInfoFuture, skuAttrFuture, skuImageFuture).get();

        return skuItemVo;
    }

//    @Override
//    public SkuItemVo item(Long skuId) {
//        SkuItemVo skuItemVo = new SkuItemVo();
//        //1、sku 的基本信息
//        SkuInfoEntity skuInfoEntity = this.getById(skuId);
//        skuItemVo.setInfo(skuInfoEntity);
//        Long spuId = skuInfoEntity.getSpuId();
//        //2、 sku 的图片信息
//        List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
//        skuItemVo.setImages(skuImagesEntities);
//        //3、获取spu的销售属性的全部组合 todo 重点功能 值得反复回味
//        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
//        skuItemVo.setSaleAttr(saleAttrVos);
//        //4、获取spu介绍
//        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
//        skuItemVo.setDesc(spuInfoDescEntity);
//        //5、获取sku规格参数
//        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,skuInfoEntity.getCatalogId());
//        skuItemVo.setGroupAttrs(attrGroupVos);
//        return skuItemVo;
//    }

}