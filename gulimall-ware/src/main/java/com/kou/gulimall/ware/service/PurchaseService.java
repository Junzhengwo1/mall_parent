package com.kou.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.ware.entity.PurchaseEntity;
import com.kou.gulimall.ware.vo.MergeVo;
import com.kou.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:29
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVo vo);

    void receivedPurchase(List<Long> ids);

    void purchaseDone(PurchaseDoneVo vo);
}

