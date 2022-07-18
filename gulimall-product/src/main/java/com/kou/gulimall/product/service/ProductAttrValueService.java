package com.kou.gulimall.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrValues(List<ProductAttrValueEntity> productAttrValueEntities);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities);
}

