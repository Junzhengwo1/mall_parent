package com.kou.gulimall.ware.controller;

import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.ware.entity.PurchaseEntity;
import com.kou.gulimall.ware.service.PurchaseService;
import com.kou.gulimall.ware.vo.MergeVo;
import com.kou.gulimall.ware.vo.PurchaseDoneVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:36:29
 */
@Api("采购信息管理")
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    /**
     * 完成采购任务
     */
    @ApiOperation("完成采购任务")
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo vo){
        purchaseService.purchaseDone(vo);
        return R.ok();
    }

    /**
     * 采购人员领取采购单
     */
    @ApiOperation("采购人员领取采购单")
    @PostMapping("/received")
    public R receivedPurchase(@RequestBody List<Long> ids){
        purchaseService.receivedPurchase(ids);
        return R.ok();
    }

    /**
     * 合并采购需求
     */
    @ApiOperation("合并采购需求")
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo vo){
        purchaseService.mergePurchase(vo);
        return R.ok();
    }

    /**
     * 列表
     */
    @ApiOperation("未领取的采购列表")
    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
