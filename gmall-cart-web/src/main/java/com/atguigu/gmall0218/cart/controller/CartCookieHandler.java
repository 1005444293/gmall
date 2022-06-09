package com.atguigu.gmall0218.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.config.CookieUtil;
import com.atguigu.gmall0218.service.ManageService;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.internal.matchers.ArrayEquals;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//实现cookie保存购物车的控制器
@Component
public class CartCookieHandler {
    //定义购物车名称
    private static String cookieCartName = "CART";
    //设置cookie过期时间
    private static int COOKIE_CART_MAXAGE = 7*24*3600;

    @Reference
    private static ManageService manageService;

    //未登录时添加购物车
    public static void addToCart(String skuId, int skuNum, String userId, HttpServletRequest request, HttpServletResponse response) {
        //判断cookie中是否有购物车，可能包含有中文，所以需要进行序列化
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean isExist = false;
        if (cartJson != null){
            //cartJson不为空，表示cookie中存在购物车信息，将当前信息实例化后取出
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    //当前商品存在于购物车中，数量相加
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //实时价格初始化
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    //将标志位变量改为true
                    isExist = true;
                }
            }
        }
        //当前商品在购物车中不存在
        if (!isExist){
            //从数据库中获取当前商品信息，并赋值给cartInfo存入cookie中
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            //赋值
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            //将数据添加到cartInfoList集合中
            cartInfoList.add(cartInfo);
        }
        //将cartInfoList集合存储到cookie中
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }


    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(String skuId, String isChecked, HttpServletRequest request, HttpServletResponse response) {
        //取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        //循环比较
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        //转换格式
        String newCartJson = JSON.toJSONString(cartList);
        //保存到cookie中
        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);
    }
}
