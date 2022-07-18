package com.kou.gulimall.ware.controller;

import com.kou.gulimall.common.exception.BizCodeEnum;
import com.kou.gulimall.common.to.SkuHasStockVo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.ware.entity.WareSkuEntity;
import com.kou.gulimall.common.exception.NoStockException;
import com.kou.gulimall.ware.service.WareSkuService;
import com.kou.gulimall.ware.vo.WareSkuLockVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:30
 */
@Api(value = "sku仓库管理",tags = "sku仓库管理")
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @ApiOperation("订单锁定库存")
    @PostMapping("/lock/order")
    public R lockOrder(@RequestBody WareSkuLockVo vo) {
        try {
            Boolean aBoolean = wareSkuService.orderLockStock(vo);
            return R.ok().setData(aBoolean);
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),e.getSkuId()+BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }

    }

    @ApiOperation("查询是否有库存")
    @PostMapping("/hasStock")
    public R queryHasStock(@RequestBody List<Long> skuIdList) {
        List<SkuHasStockVo> vos = wareSkuService.queryHasStock(skuIdList);
        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @ApiOperation("商品Sku列表")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
