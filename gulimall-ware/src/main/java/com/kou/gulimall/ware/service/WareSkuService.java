package com.kou.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.to.SkuHasStockVo;
import com.kou.gulimall.common.to.mq.OrderTo;
import com.kou.gulimall.common.to.mq.StockLockedTo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.ware.entity.WareSkuEntity;
import com.kou.gulimall.ware.vo.WareSkuLockVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:30
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> queryHasStock(List<Long> skuIdList);

    /**
     * 锁库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);


    void releaseLockStock(StockLockedTo to) throws IOException;

    void releaseLockStock(OrderTo to);
}

