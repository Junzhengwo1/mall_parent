package com.kou.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.order.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:31:34
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

