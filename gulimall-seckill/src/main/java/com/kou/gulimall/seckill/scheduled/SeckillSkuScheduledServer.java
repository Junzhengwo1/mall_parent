package com.kou.gulimall.seckill.scheduled;

import com.kou.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品定时上架
 * 每天晚上3点，上架最新三天的商品
 * 时间段 00：00：00 - 23：59：59
 */

@Slf4j
@Service
public class SeckillSkuScheduledServer {

    @Autowired
    private SeckillService seckillService;

    @Scheduled(cron = "0 * * * * ?")
    public void uploadSecKillSkuLatest3Days(){
        //1、重复上架无需处理
        log.info("开始上架…………………………………………");
        seckillService.uploadSecKillSkuLatest3Days();
    }
}
