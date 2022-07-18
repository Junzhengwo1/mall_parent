package com.kou.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.order.entity.OrderEntity;
import com.kou.gulimall.order.vo.OrderConfirmVo;
import com.kou.gulimall.order.vo.OrderSubmitVo;
import com.kou.gulimall.order.vo.SubmitOrderRespVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:31:34
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认信息
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 提交订单 入库
     * @param vo
     * @return
     */
    SubmitOrderRespVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity orderEntity);
}

