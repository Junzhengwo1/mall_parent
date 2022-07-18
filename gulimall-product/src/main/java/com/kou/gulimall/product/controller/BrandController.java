package com.kou.gulimall.product.controller;

import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.common.valid.AddGroup;
import com.kou.gulimall.common.valid.UpdateGroup;
import com.kou.gulimall.product.entity.BrandEntity;
import com.kou.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;





/**
 * 品牌
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-22 16:20:09
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @PostMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     *
     * todo 这里通过oss对象存储来将图片上传上去
     * TODO 签名后上传
     * 保存的实体对象，我们来个统一处理
     * 使用@ControllerAdvice
     * 保存
     */
    @RequestMapping("/save") //后面这个就是校验的结果对象
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*,BindingResult result*/){
//		if (result.hasErrors()) {
//		    Map<String,String> map = new HashMap<>();
//		    //1.获取校验的结果
//            result.getFieldErrors().forEach(item -> {
//                //获取的错误信息
//                String message = item.getDefaultMessage();
//                //获取错误的属性的名字
//                String field = item.getField();
//                map.put(field,message);
//            });
//            return R.error(400,"提交信息不合法").put("data",map);
//        } else {
//            brandService.save(brand);
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class})@RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
