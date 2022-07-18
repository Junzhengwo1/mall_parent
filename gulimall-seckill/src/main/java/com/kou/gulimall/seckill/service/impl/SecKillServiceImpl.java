package com.kou.gulimall.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.seckill.feign.CouponFeignService;
import com.kou.gulimall.seckill.feign.ProductFeignService;
import com.kou.gulimall.seckill.service.SeckillService;
import com.kou.gulimall.seckill.to.SeckillSkuRedisTo;
import com.kou.gulimall.seckill.vo.SecKillSessionWithSkus;
import com.kou.gulimall.seckill.vo.SkuInfoEntityVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecKillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    private final String SKU_KILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//"+商品随机码

    @Override
    public void uploadSecKillSkuLatest3Days() {
        //1.扫描最近三天需要参与秒杀活动的商品信息
        R latest3DaySession = couponFeignService.getLatest3DaySession();
        if (latest3DaySession.getCode()==0){
            //上架
            List<SecKillSessionWithSkus> data = latest3DaySession.getData("data", new TypeReference<List<SecKillSessionWithSkus>>() {});
            //缓存到redis
            //1.缓存活动信息
            this.saveSessionInfos(data);
            //2.缓存活动的关联商品信息；
            this.saveSessionSkuInfos(data);
        }
    }

    private void saveSessionSkuInfos(List<SecKillSessionWithSkus> data) {
        data.forEach(o->{
            long startTime = o.getStartTime().getTime();
            long endTime = o.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX+startTime+"_"+endTime;
            List<String> ids = o.getSeckillSkuRelationEntities().stream().map(e -> e.getId().toString()).collect(Collectors.toList());
            //缓存活动信息
            redisTemplate.opsForList().leftPushAll(key,ids);
        });
    }

    private void saveSessionInfos(List<SecKillSessionWithSkus> data) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
        data.forEach(o->{
            o.getSeckillSkuRelationEntities().forEach(e->{
                //缓存商品信息
                //1.sku的基本信息
                SeckillSkuRedisTo to = new SeckillSkuRedisTo();
                R skuInfo = productFeignService.getSkuInfo(e.getSkuId());
                if(skuInfo.getCode()==0){
                    SkuInfoEntityVo skuInfoData = skuInfo.getData("skuInfo", new TypeReference<SkuInfoEntityVo>() {});
                    to.setSkuInfoEntityVo(skuInfoData);
                }
                //2.sku秒杀信息
                BeanUtil.copyProperties(e,to);
                //3.设置开始结束日期
                to.setStartTime(o.getStartTime().getTime()).setEndTime(o.getEndTime().getTime());
                //4.随机码？（）
                to.setRandCode(UUID.randomUUID().toString().replace("_", ""));
                String toString = JSON.toJSONString(to);
                //5.分布式信号量
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE);
                //商品可以秒杀的数量作为信号量 （限流）
                semaphore.trySetPermits(e.getSeckillCount());
                ops.put(e.getId().toString(),toString);

            });
        });
    }
}
