package com.kou.gulimall.product.controller.web;


import com.kou.gulimall.product.entity.CategoryEntity;
import com.kou.gulimall.product.service.CategoryService;
import com.kou.gulimall.product.vo.CateLog2Vo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Api(value = "商城主页",tags = "商城主页")
@Controller
public class IndexController {


    @Autowired
    private CategoryService categoryService;

    @Autowired
    @Qualifier("redisTemplateMy")
    private RedisTemplate<String,Object> redisTemplateMy;

    @Autowired
    private RedissonClient redissonClient;

    @ApiOperation("主页 | 一级分类目录")
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //查出所有的一级分类的目录
        List<CategoryEntity> categoryEntities = categoryService.getLevelOneCategroys ();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }



    @ApiOperation("三级分类")
    @ResponseBody //x将我们的值以JSON的形式提交出去
    @GetMapping("/index/catalog.json")
    public Map<String, List<CateLog2Vo>> getCatalogJson(){
        return categoryService.getCatalogJson();
    }



    @ApiOperation("写数据")
    @ResponseBody
    @GetMapping("/write")
    public String writeValue(){
        //模拟读写锁使用场景
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30);
            redisTemplateMy.opsForValue().set("write",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;

    }

    @ApiOperation("读数据")
    @ResponseBody
    @GetMapping("/read")
    public String readValue(){
        //模拟读写锁使用场景
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");

        String s = "";
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            s=redisTemplateMy.opsForValue().get("write").toString();
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;

    }


}
