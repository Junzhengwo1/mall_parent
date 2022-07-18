package com.kou.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.ware.entity.WareInfoEntity;
import com.kou.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:30
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收获地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

