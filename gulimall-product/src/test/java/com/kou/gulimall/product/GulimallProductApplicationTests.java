package com.kou.gulimall.product;


import com.kou.gulimall.product.dao.AttrGroupDao;
import com.kou.gulimall.product.dao.SkuSaleAttrValueDao;
import com.kou.gulimall.product.entity.BrandEntity;
import com.kou.gulimall.product.service.BrandService;
import com.kou.gulimall.product.service.CategoryService;
import com.kou.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;


    @Autowired
    private CategoryService categoryService;


    @Autowired
    @Qualifier("redisTemplateMy")
    private RedisTemplate<String,Object> redisTemplateMy;


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrGroupDao attrGroupDao;


    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    void get(){
        skuSaleAttrValueDao.getSaleAttrsBySpuId(13L).forEach(System.out::println);
    }

    @Test
    void getAttrGroupWithAttrsBySpuId(){
        List<SpuItemAttrGroupVo> vos = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
        vos.forEach(System.out::println);

    }




    /**
     * redisson测试
     */

    @Test
    void redissonTest(){

        System.out.println(redissonClient);
    }

    /**
     * redis 测试
     */
    @Test
    void redisTest(){
        redisTemplateMy.opsForValue().set("kou","king");
       // String catalogJson = redisTemplateMy.opsForValue().get("catalogJson").toString();
        Object categroys = redisTemplateMy.opsForValue().get("kou");
        System.out.println(categroys);
        //System.out.println(catalogJson);
    }



    @Test
    void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("king");
        brandEntity.setDescript("abc");
        brandService.save(brandEntity);

        brandService.list().forEach(System.out::println);

    }


    @Test
    void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        for (Long aLong : catelogPath) {
            System.out.println(aLong);
        }
    }




}
