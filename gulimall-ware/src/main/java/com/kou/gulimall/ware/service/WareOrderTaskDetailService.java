package com.kou.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:30
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

