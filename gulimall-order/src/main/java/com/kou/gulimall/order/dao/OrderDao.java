package com.kou.gulimall.order.dao;

import com.kou.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:31:34
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
