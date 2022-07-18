package com.kou.gulimall.cart.controller;


import com.kou.gulimall.cart.service.CartService;
import com.kou.gulimall.cart.vo.CartItem;
import com.kou.gulimall.cart.vo.CartVo;
import com.kou.gulimall.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Api(tags = "购物车相关")
@RestController
public class CartController {

    @Autowired
    private CartService cartService;


    @ApiOperation("获取当前用户的购物项")
    @GetMapping("cart/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

    /**
     * cookie user-key 表示用户的身份，一个月后才过期
     * 第一次 如果没有临时用户，先创建一个临时用户 -》 使用拦截器来实现
     * todo 这里需要判断用户是否是登录的用户 从分布式session获取的数据
     * @return
     * 拦截器的目标方法
     */
    @ApiOperation("购物车列表信息")
    @GetMapping("cart/listPage")
    public R cartListPage() throws ExecutionException, InterruptedException {
        //todo 这里区别了临时购物车还是登录人的购物车
        // 【这里也是cookie的应用亮点】
        CartVo cartVo = cartService.getCart();
        return R.ok().put("data",cartVo);
    }

    @ApiOperation("添加商品")
    @GetMapping("cart/addToCart")
    public R addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId,num);
        return R.ok().put("data",cartItem);
    }

    @ApiOperation("获取购物车信息")
    @GetMapping("cart/getCartItem")
    public R getCartItem(@RequestParam("skuId") Long skuId){
        CartItem cartItem = cartService.getCartItem(skuId);
        return R.ok().put("data",cartItem);
    }

    @ApiOperation("选中商品")
    @GetMapping("cart/checkItem")
    public R checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return R.ok();
    }

    @ApiOperation("商品数量")
    @GetMapping("cart/countItem")
    public R countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return R.ok();
    }

    @ApiOperation("删除购物项")
    @GetMapping("cart/deleteItem")
    public R deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return R.ok();
    }


}
