package com.kou.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 19:51:57
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

