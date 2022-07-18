package com.kou.gulimall.product.controller;

import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.entity.SpuInfoEntity;
import com.kou.gulimall.product.service.SpuInfoService;
import com.kou.gulimall.product.vo.SpuSaveVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;





/**
 * spu信息
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Api(value = "spu信息",tags = "spu信息")
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {

    @Autowired
    private SpuInfoService spuInfoService;

    @ApiOperation(value = "Spu信息forSkuId")
    @GetMapping("/bySkuId/{skuId}")
    public R SpuInfoForSkuId(@PathVariable("skuId") Long skuId ){
        SpuInfoEntity spuInfoEntity = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(spuInfoEntity);
    }


    @ApiOperation(value = "商品上架功能")
    @PostMapping("/{spuId}/up")
    public R productUp(@PathVariable("spuId") Long spuId){
        spuInfoService.productUp(spuId);
        return R.ok();
    }


    /**
     * 列表 | 带检索条件的列表查询
     */
    @PostMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     * 校验时使用JSR303校验
     */
    @ApiOperation("spu保存")
    @PostMapping("/save")
    public R save(@RequestBody SpuSaveVo vo){
		//spuInfoService.save(vo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
