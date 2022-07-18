package com.kou.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.coupon.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 19:51:57
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

