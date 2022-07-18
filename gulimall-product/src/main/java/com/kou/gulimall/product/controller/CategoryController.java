package com.kou.gulimall.product.controller;

import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.product.entity.CategoryEntity;
import com.kou.gulimall.product.service.CategoryService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;





/**
 * TODO 其中
 * todo 1、树结构展示
 * todo 2、拖拽功能 值得学习
 * 商品三级分类
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@Api(tags = "三级分类")
@RefreshScope
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * TODO 三级目录结构展示
     * 列表
     */
    @PostMapping("/list/tree")
    public R listWithTree(){
        List<CategoryEntity> trees = categoryService.listWithTree();
        return R.ok().put("trees", trees);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);
        return R.ok();
    }


    /**
     * 修改排序
     */
    @PostMapping("/batchUpdate/sort")
    public R batchUpdate(@RequestBody List<CategoryEntity> categorys){
        categoryService.updateBatchById(categorys);
        return R.ok();
    }


    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
		//categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
