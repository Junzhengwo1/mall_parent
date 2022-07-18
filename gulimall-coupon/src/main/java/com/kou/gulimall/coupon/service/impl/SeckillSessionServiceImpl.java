package com.kou.gulimall.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.coupon.dao.SeckillSessionDao;
import com.kou.gulimall.coupon.entity.SeckillSessionEntity;
import com.kou.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.kou.gulimall.coupon.service.SeckillSessionService;
import com.kou.gulimall.coupon.service.SeckillSkuRelationService;
import com.kou.gulimall.coupon.vo.SeckillSessionEntityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntityVo> getLatest3DaySession() {
        String startTime = this.getCondtionTime(LocalTime.MIN);
        String endTime = this.getCondtionTime(LocalTime.MAX);
        List<SeckillSessionEntity> list = this.list(new LambdaQueryWrapper<SeckillSessionEntity>().between(SeckillSessionEntity::getStartTime, startTime, endTime));
        if(CollectionUtil.isNotEmpty(list)){
            return list.stream().map(o -> {
                SeckillSessionEntityVo vo = new SeckillSessionEntityVo();
                BeanUtil.copyProperties(o, vo);
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new LambdaQueryWrapper<SeckillSkuRelationEntity>().eq(SeckillSkuRelationEntity::getPromotionSessionId, o.getId()));
                vo.setSeckillSkuRelationEntities(relationEntities);
                return vo;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private String getCondtionTime(LocalTime time) {
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(Duration.ofDays(3).toDays());
        LocalDateTime localDateTime = LocalDateTime.of(localDate, time);
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}