package com.kou.gulimall.coupon.vo;

import com.kou.gulimall.coupon.entity.SeckillSessionEntity;
import com.kou.gulimall.coupon.entity.SeckillSkuRelationEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SeckillSessionEntityVo extends SeckillSessionEntity {

    private List<SeckillSkuRelationEntity> seckillSkuRelationEntities;
}
