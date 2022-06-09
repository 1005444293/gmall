package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 将商品添加到购物车
     * @param skuId
     * @param parseInt
     * @param userId
     */
    void addToCart(String skuId, int parseInt, String userId);

    /**
     * 将数据库或者缓存中的购物车的数据展示出来
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 当用户登录后，将cookie中的购物车信息合并到数据库中，并更新缓存
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    /**
     * 在缓存中生成另一个购物车，将用户勾选的商品存储到次购物车中
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 获取到缓存中被选中的商品
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
