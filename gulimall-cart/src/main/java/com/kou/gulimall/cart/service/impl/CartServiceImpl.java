package com.kou.gulimall.cart.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.kou.gulimall.cart.feign.ProductFeignService;
import com.kou.gulimall.cart.interceptor.CartIntercepter;
import com.kou.gulimall.cart.service.CartService;
import com.kou.gulimall.cart.vo.CartItem;
import com.kou.gulimall.cart.vo.CartVo;
import com.kou.gulimall.cart.vo.SkuInfoVo;
import com.kou.gulimall.cart.vo.UserInfoTo;
import com.kou.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final String CART_PREFIX = "gulimall:cart";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;



    /**
     * 本质上是操作redis
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartIOps = this.getCartIOps();
        //先判断redis中是否以及有该商品了
        String sv = (String) cartIOps.get(skuId.toString());
        if(StrUtil.isEmpty(sv)){
            CartItem cartItem = new CartItem();
            //购物无此商品
            //远程查询当前需要添加的商品-异步查询
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R info = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                //开始添加操作
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImge(skuInfo.getSkuDefaultImg());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setTotalPrice(cartItem.calTotalPrice());
            },executor);

            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                //远程查询sku的组合信息
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask,getSkuSaleAttrValues).get();
            String s = JSON.toJSONString(cartItem);
            cartIOps.put(skuId.toString(), s);
            return cartItem;
        }
        else {
            //购物车有此商品
            CartItem cartItem = JSON.parseObject(sv, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            cartIOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartIOps = this.getCartIOps();
        String o = (String) cartIOps.get(skuId.toString());
        return JSON.parseObject(o,CartItem.class);
    }

    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        //区分登录状态还是未登录状态
        UserInfoTo userInfoTo = CartIntercepter.toThreadLocal.get();
        if(ObjectUtil.isNotNull(userInfoTo) && userInfoTo.getUserId()!=null){
            //登录状态的
            String realcartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempcartKey = CART_PREFIX + userInfoTo.getUserKey();
            //如果临时购物车有数据的话 需要合并起来
            List<CartItem> tempcartItems = this.getCartItems(tempcartKey);
            if(tempcartItems != null){
                //合并操作
                for (CartItem tempcartItem : tempcartItems) { //增强for 无法抛异常出去
                    this.addToCart(tempcartItem.getSkuId(), tempcartItem.getCount());
                }
                //清空购物车
                this.clearCart(tempcartKey);
            }
            List<CartItem> cartItems = this.getCartItems(realcartKey);
            cartVo.setCartItems(cartItems);

        }else {
            //未登录状态
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //临时购物车
            List<CartItem> cartItems = this.getCartItems(cartKey);
            cartVo.setCartItems(cartItems);
        }

        return cartVo;

    }

    @Override
    public void clearCart(String tempcartKey) {
        redisTemplate.delete(tempcartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartIOps = this.getCartIOps();
        CartItem cartItem = this.getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartIOps.put(skuId.toString(),s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = this.getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartIOps = this.getCartIOps();
        cartIOps.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartIOps = this.getCartIOps();
        cartIOps.delete(skuId.toString());
    }


    @Override
    @SuppressWarnings("all")
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartIntercepter.toThreadLocal.get();
        if(userInfoTo.getUserId()==null){
            return Lists.newArrayList();
        }else {
            String cartKey = CART_PREFIX+userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            //过滤 只要选中的购物项目
            if(cartItems != null){
                return cartItems.stream().filter(CartItem::getCheck)
                        //更新最新价格
                        .map(o->{
                            BigDecimal realTimePrice = productFeignService.getRealTimePrice(o.getSkuId());
                            o.setPrice(realTimePrice);
                            return o;
                        }).collect(Collectors.toList());
            }else {
                return Lists.newArrayList();
            }

        }

    }


    /**
     * todo 获取到购物车 一个redis 对象
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartIOps() {
        UserInfoTo userInfoTo = CartIntercepter.toThreadLocal.get(); //模拟的用户信息
        String cartKey = "";
        if(ObjectUtil.isNotNull(userInfoTo) && userInfoTo.getUserId()!=null){
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }else {
            cartKey = CART_PREFIX+userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    @SuppressWarnings("all")
    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if(CollectionUtil.isNotEmpty(values)){
            List<CartItem> collect = values.stream().map(o -> {
                String str = (String) o;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }



}
