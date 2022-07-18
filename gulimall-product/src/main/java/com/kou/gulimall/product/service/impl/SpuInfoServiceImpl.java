package com.kou.gulimall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.constant.ProductConstant;
import com.kou.gulimall.common.to.SkuEsModel;
import com.kou.gulimall.common.to.SkuHasStockVo;
import com.kou.gulimall.common.to.SkuReductionTo;
import com.kou.gulimall.common.to.SpuBoundsTo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.dao.SpuInfoDao;
import com.kou.gulimall.product.entity.*;
import com.kou.gulimall.product.feign.CouponFeignService;
import com.kou.gulimall.product.feign.SearchFeignService;
import com.kou.gulimall.product.feign.WareFeignService;
import com.kou.gulimall.product.service.*;
import com.kou.gulimall.product.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;


    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 大对象保存
     * TODO 其中 还有很多比如失败后数据回滚相关的问题
     * TODO 在高级部分讲解 完善 2021/12/21 完善了调用远程服务的事务问题 使用seata的分布式事务
     * @param vo 前端传来的数据
     */
    @GlobalTransactional//2021/12/21 添加该注解解决分布式事务的遗留问题
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu的基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(DateUtil.date());
        spuInfoEntity.setUpdateTime(DateUtil.date());
        this.saveBaseSpuInfo(spuInfoEntity);
        //2、保存spu的描述图片 pms_spu_info_desc
        List<String> decripts = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(StrUtil.join(";",decripts));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4、保存spu的规格参数；pms_product_attr_value
        List<BaseAttrsVo> baseAttrsVos = vo.getBaseAttrsVos();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrsVos.stream().map(o -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            BeanUtils.copyProperties(o, productAttrValueEntity);
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            String attrName = attrService.getById(o.getAttrId()).getAttrName();
            productAttrValueEntity.setAttrName(attrName);
            productAttrValueEntity.setQuickShow(o.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveAttrValues(productAttrValueEntities);
        //5、保存当前spu的sku信息 :
        //5.1:
        List<SkusVo> skusVos = vo.getSkusVos();
        if(CollectionUtil.isEmpty(skusVos)){
            skusVos.forEach(sku->{
                String defaultImg = "";
                for (ImagesVo imagesVo : sku.getImagesVos()) {
                    if(imagesVo.getDefaultImg()==1){
                        defaultImg = imagesVo.getImgUrl();
                    }
                }
                // private String skuName;
                // private BigDecimal peice;
                // private String skuTitle;
                // private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();//保存之后的skuId
                List<ImagesVo> imagesVos = sku.getImagesVos();
                List<SkuImagesEntity> skuImagesEntities = imagesVos.stream().map(imagesVo -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(imagesVo, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter(e-> Optional.ofNullable(e.getImgUrl()).isPresent()).collect(Collectors.toList());
                //TODO 没有图片路径的不入库
                skuImagesService.saveBatchSkuImages(skuImagesEntities);
                List<AttrForSpuVo> attrForSpuVos = sku.getAttrForSpuVos();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrForSpuVos.stream().map(o -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(o, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatchSaleAttrValue(skuSaleAttrValueEntities);

                //  这下面的都是操作远程服务的保存（积分、）
                BoundsVo boundsVo = vo.getBoundsVo();
                SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
                BeanUtils.copyProperties(boundsVo,spuBoundsTo);
                spuBoundsTo.setSpuId(spuInfoEntity.getId());
                R r = couponFeignService.saveSpuBounds(spuBoundsTo);
                if(r.getCode() != 0){
                    log.error("远程保存spu积分信息失败-{}");
                }
                // 优惠信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().doubleValue()>0){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存spu满减信息失败-{}");
                    }
                }
            });
        }

    }


    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = Wrappers.lambdaQuery();
        String key = (String) params.get("key");
        if(StrUtil.isNotBlank(key)){
            queryWrapper.and(o->{
                o.eq(SpuInfoEntity::getId,key).or().like(SpuInfoEntity::getSpuName,key);
            });
        }
        String status = (String) params.get("status");
        if(StrUtil.isNotBlank(status)){
            queryWrapper.eq(SpuInfoEntity::getPublishStatus,status);
        }
        String brandId = (String) params.get("brandId");
        if(StrUtil.isNotBlank(brandId)){
            queryWrapper.eq(SpuInfoEntity::getBrandId,brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if(StrUtil.isNotBlank(catelogId)){
            queryWrapper.eq(SpuInfoEntity::getCatalogId,catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    /**
     * 商品上架
     * @param spuId spuId
     */
    @Transactional
    @Override
    public void productUp(Long spuId) {

        //1、查询当前spuId 所对应的所有sku信息，以及品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // 4、查询当前sku所有可以用来被检索的属性
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = attrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrService.listByIds(attrIds);
        List<AttrEntity> collect = attrEntities.stream().filter(o -> o.getSearchType().equals(ProductConstant.AttrEnum.SEARCHTYPE.getCode())).collect(Collectors.toList());
        List<Long> longs = collect.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());
        List<ProductAttrValueEntity> attrValueEntityList = attrValueEntities.stream().filter(o -> longs.contains(o.getAttrId())).collect(Collectors.toList());
        List<SkuEsModel.Attrs> attrs = attrValueEntityList.stream().map(o -> {
            SkuEsModel.Attrs attrsForEs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(o, attrsForEs);
            return attrsForEs;
        }).collect(Collectors.toList());

        // 1、发送远程调用，库存系统查询是否有库存
        Map<Long,Boolean> stockMap = null;
        try {
            R r = wareFeignService.queryHasStock(skuIds);
            List<SkuHasStockVo> vos = r.getData("data",new TypeReference<List<SkuHasStockVo>>() {});//todo 重点应用场景
            stockMap = vos.stream().collect(toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("远程调用查看库存异常：原因{}",e);
        }
        //2、封装每一个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModelList = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtil.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //设置的就是map中的vaL true OR false
            if (CollectionUtil.isEmpty(finalStockMap)){
                skuEsModel.setHasStock(false);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //2、热度评分,默认 0
            skuEsModel.setHotScore(0L);
            // 3、查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatelogName(categoryEntity.getName());
            skuEsModel.setAttrs(attrs);
            return skuEsModel;
        }).collect(Collectors.toList());

        // 5、将数据发送给es保存
        R r = searchFeignService.productStatusUp(skuEsModelList);
        if (0 == r.getCode()){
            //远程调用成功
            //todo 6、修改当前spu的状态为发布状态
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.SPU_UP.getCode());
            spuInfoEntity.setUpdateTime(DateUtil.date());
            this.updateById(spuInfoEntity);
        }else {
            //远程调用失败
            //TODO 7、重复调用，接口幂等性，重试机制


        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        return this.getById(skuInfoEntity.getSpuId());
    }


}