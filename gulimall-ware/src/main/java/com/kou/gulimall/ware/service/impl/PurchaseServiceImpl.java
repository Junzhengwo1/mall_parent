package com.kou.gulimall.ware.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.constant.WareConstant;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.ware.dao.PurchaseDao;
import com.kou.gulimall.ware.entity.PurchaseDetailEntity;
import com.kou.gulimall.ware.entity.PurchaseEntity;
import com.kou.gulimall.ware.service.PurchaseDetailService;
import com.kou.gulimall.ware.service.PurchaseService;
import com.kou.gulimall.ware.service.WareSkuService;
import com.kou.gulimall.ware.vo.MergeVo;
import com.kou.gulimall.ware.vo.PurchaseDoneVo;
import com.kou.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = Wrappers.lambdaQuery();
        //String status = (String) params.get("status");
        queryWrapper.eq(PurchaseEntity::getStatus, BigDecimal.ZERO.intValue()).or().eq(PurchaseEntity::getStatus, BigDecimal.ONE.intValue());
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        if (ObjectUtil.isEmpty(purchaseId)) {
            //新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(DateUtil.date());
            purchaseEntity.setUpdateTime(DateUtil.date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // TODO 确认采购单状态是0，1才可以合并

        List<Long> items = vo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(o -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(o);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(DateUtil.date());
        this.updateById(purchaseEntity);
    }

    /**
     * @param ids 采购单的ids
     */
    @Transactional
    @Override
    public void receivedPurchase(List<Long> ids) {
        //1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = this.listByIds(ids);
        //最终想要处理的采购单
        List<PurchaseEntity> results = purchaseEntities.stream()
                .filter(o -> o.getStatus().equals(WareConstant.PurchaseStatusEnum.CREATED.getCode())
                        || o.getStatus().equals(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()))
                .map(o -> {
                    o.setStatus(WareConstant.PurchaseStatusEnum.RECEVICE.getCode());
                    o.setUpdateTime(DateUtil.date());
                    return o;
                })//过滤结果后设置最新的状态
                .collect(Collectors.toList());
        //2、修改对应采购单的状态
        this.updateBatchById(results);
        //3、改变采购项状态
        results.forEach(o -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailPurchaseId(o.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                BeanUtils.copyProperties(entity, purchaseDetailEntity);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        });


    }

    @Transactional
    @Override
    public void purchaseDone(PurchaseDoneVo vo) {
        //1、改变采购单项的状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();
        List<PurchaseDetailEntity> updateds = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus().equals(WareConstant.PurchaseDetailStatusEnum.HASEEOR.getCode())) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3、将成功采购的进行入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            updateds.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updateds);

        //2、改变采购单的状态 | 采购项都完成的状态下
        @NotNull Long purchaseId = vo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASEEOR.getCode());
        purchaseEntity.setUpdateTime(DateUtil.date());
        this.updateById(purchaseEntity);


    }

}