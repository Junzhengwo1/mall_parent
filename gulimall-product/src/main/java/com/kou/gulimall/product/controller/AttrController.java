package com.kou.gulimall.product.controller;

import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.entity.ProductAttrValueEntity;
import com.kou.gulimall.product.service.AttrService;
import com.kou.gulimall.product.service.ProductAttrValueService;
import com.kou.gulimall.product.vo.AttrRespVo;
import com.kou.gulimall.product.vo.AttrVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;





/**
 * 商品属性
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Api(value = "属性管理",tags = "属性管理")
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 获取商品的基本属性 | 销售属性
     * @param params 分页蚕食
     * @param catelogId 分类ID
     * @return 响应消息
     */
    @ApiOperation("获取商品的基本属性 | 销售属性")
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId,@PathVariable("attrType") String attrType){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page", page);
    }


    /**
     * 获取spu规格
     */
    @ApiOperation("获取spu规格")
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> entityList = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", entityList);
    }





    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @ApiOperation("spu信息")
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("data", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo vo){
		attrService.saveAttr(vo);

        return R.ok();
    }






    /**
     * 修改
     */
    @ApiOperation("spu信息修改")
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> productAttrValueEntities){
        productAttrValueService.updateSpuAttr(spuId,productAttrValueEntities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
