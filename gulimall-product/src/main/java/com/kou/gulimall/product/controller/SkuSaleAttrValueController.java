package com.kou.gulimall.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.kou.gulimall.product.service.SkuSaleAttrValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;





/**
 * sku销售属性&值
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Api(tags = "sku销售属性相关")
@RestController
@RequestMapping("product/skusaleattrvalue")
public class SkuSaleAttrValueController {
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;



    @ApiOperation("销售属性列表")
    @GetMapping("/stringList{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId){
        List<SkuSaleAttrValueEntity> list = skuSaleAttrValueService.list(new LambdaQueryWrapper<SkuSaleAttrValueEntity>().eq(SkuSaleAttrValueEntity::getSkuId, skuId));
        List<String> strings = Lists.newArrayList();
        list.forEach(o->{
            String join = String.join(":", o.getAttrName(), o.getAttrValue());
            strings.add(join);
        });
        return strings;
    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuSaleAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SkuSaleAttrValueEntity skuSaleAttrValue = skuSaleAttrValueService.getById(id);

        return R.ok().put("skuSaleAttrValue", skuSaleAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.save(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.updateById(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		skuSaleAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
