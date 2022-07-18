package com.kou.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.to.MemberPriceVo;
import com.kou.gulimall.common.to.SkuReductionTo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.coupon.dao.SkuFullReductionDao;
import com.kou.gulimall.coupon.entity.MemberPriceEntity;
import com.kou.gulimall.coupon.entity.SkuFullReductionEntity;
import com.kou.gulimall.coupon.entity.SkuLadderEntity;
import com.kou.gulimall.coupon.service.MemberPriceService;
import com.kou.gulimall.coupon.service.SkuFullReductionService;
import com.kou.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo to) {
        //1、保存满减打折，会员价
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(to,skuLadderEntity);
        skuLadderEntity.setAddOther(to.getCountStatus());
        // todo 在下订单的时候计算折后价
        if(to.getFullCount()>0){
            skuLadderService.save(skuLadderEntity);
        }

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(to,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(to.getCountStatus());
        if(to.getFullPrice().doubleValue()>0){
            this.save(skuFullReductionEntity);
        }

        List<MemberPriceVo> memberPriceVos = to.getMemberPriceVos();
        List<MemberPriceEntity> memberPriceEntities = memberPriceVos.stream().map(o -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(to.getSkuId());
            memberPriceEntity.setMemberLevelId(o.getId());
            memberPriceEntity.setMemberLevelName(o.getName());
            memberPriceEntity.setMemberPrice(o.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(e-> Optional.ofNullable(e.getMemberPrice()).isPresent() && e.getMemberPrice().doubleValue() > 0)
          .collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);

    }

}