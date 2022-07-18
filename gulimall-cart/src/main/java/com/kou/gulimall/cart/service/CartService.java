package com.kou.gulimall.cart.service;

import com.kou.gulimall.cart.vo.CartItem;
import com.kou.gulimall.cart.vo.CartVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public interface CartService {

    /**
     * 添加到购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     * 区分是否是临时购物车
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartVo getCart() throws ExecutionException, InterruptedException;


    /**
     * 清空购物城
     * @param tempcartKey
     */
    void clearCart(String tempcartKey);

    /**
     * 购物项勾选
     * @param skuId
     * @param check
     * @return
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获取当前登录用户的购物项（已经选中的）
     * @return
     */
    List<CartItem> getCurrentUserCartItems();
}
