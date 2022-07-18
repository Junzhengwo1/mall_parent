package com.kou.gulimall.product.controller;

import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.entity.AttrEntity;
import com.kou.gulimall.product.entity.AttrGroupEntity;
import com.kou.gulimall.product.service.AttrAttrgroupRelationService;
import com.kou.gulimall.product.service.AttrGroupService;
import com.kou.gulimall.product.service.AttrService;
import com.kou.gulimall.product.service.CategoryService;
import com.kou.gulimall.product.vo.AttrGroupRelationVo;
import com.kou.gulimall.product.vo.AttrGroupWithAttrsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;





/**
 * 属性分组
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Api(value = "属性分组",tags = "属性分组")
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;



    @ApiOperation("获取分类下所有分组&关联的属性")
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable("catelogId") Long catelogId){
        //1、查询当前分类下的所有属性分组
        //2、查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos =  attrGroupService.getAttrGroupWithAttrsByCateId(catelogId);
        return R.ok().put("data",vos);
    }

    @ApiOperation("获取已关联的属性")
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrGroupId){
        List<AttrEntity> entities =  attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data",entities);
    }


    @ApiOperation("获取未关联的属性")
    @GetMapping("/{attrgroupId}/nonattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params
                            ,@PathVariable("attrgroupId") Long attrGroupId){
        PageUtils page = attrService.getNoRelationAttr(params,attrGroupId);
        return R.ok().put("data",page);
    }

    @ApiOperation("新增关联关系")
    @PostMapping("/attr/addRelation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.addBatchRelation(vos);
        return R.ok();
    }


    /**
     * 列表
     */
    @GetMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		// 当前层级ID
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }


    @ApiOperation("删除属性关联")
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrGroupRelationVo> vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
