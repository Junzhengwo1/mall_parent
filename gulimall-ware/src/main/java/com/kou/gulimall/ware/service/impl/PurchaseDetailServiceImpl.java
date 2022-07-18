package com.kou.gulimall.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.ware.dao.PurchaseDetailDao;
import com.kou.gulimall.ware.entity.PurchaseDetailEntity;
import com.kou.gulimall.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if(StrUtil.isNotBlank(key)){
            queryWrapper.and(w->w.eq(PurchaseDetailEntity::getSkuId,key).or().like(PurchaseDetailEntity::getSkuNum,key));
        }
        String status = (String) params.get("status");
        if (StrUtil.isNotBlank(status)) {
            queryWrapper.eq(PurchaseDetailEntity::getStatus,status);
        }
        String wareId = (String) params.get("wareId");
        if (StrUtil.isNotBlank(wareId)) {
            queryWrapper.eq(PurchaseDetailEntity::getWareId,wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);

    }

    @Override
    public List<PurchaseDetailEntity> listDetailPurchaseId(Long id) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PurchaseDetailEntity::getPurchaseId, id);
        List<PurchaseDetailEntity> purchaseDetailEntities = this.list(queryWrapper);
        return purchaseDetailEntities;
    }

}