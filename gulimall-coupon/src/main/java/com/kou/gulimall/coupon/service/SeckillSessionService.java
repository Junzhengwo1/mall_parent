package com.kou.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.coupon.entity.SeckillSessionEntity;
import com.kou.gulimall.coupon.vo.SeckillSessionEntityVo;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 19:51:57
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 最近三天的活动
     * @return
     */
    List<SeckillSessionEntityVo> getLatest3DaySession();
}

