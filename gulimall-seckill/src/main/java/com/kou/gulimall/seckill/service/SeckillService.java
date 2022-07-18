package com.kou.gulimall.seckill.service;

import org.springframework.stereotype.Service;

@Service
public interface SeckillService {
    /**
     * 上架最近三天的需要秒杀的活动
     */
    void uploadSecKillSkuLatest3Days();

}
