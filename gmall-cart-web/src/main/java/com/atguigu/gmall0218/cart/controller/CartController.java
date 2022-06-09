package com.atguigu.gmall0218.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.config.CookieUtil;
import com.atguigu.gmall0218.config.LoginRequire;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    //添加购物车时，需要区分用户是否登录，如果登录，则将商品添加进数据库和缓存，如果未登录，将商品添加进cookie中
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //获取商品信息
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //获取用户信息，判断用户是否登录
        String userId = (String) request.getAttribute("userId");
        if (userId != null){
            //此时表明用户登录了
            cartService.addToCart(skuId, Integer.parseInt(skuNum), userId);
        }else {
            //此时表明用户未登录，将数据添加到cookie中
            cartCookieHandler.addToCart(skuId, Integer.parseInt(skuNum), userId, request, response);
        }
        //获取添加购物车的相关信息，展示在前端页面上
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response){
        //查看用户是否登录
        String userId = (String) request.getAttribute("userId");
        if (userId != null){
            //用户已经登录，可以从数据库或缓存中取数据
            //合并购物车数据到数据库中
            List<CartInfo> cartList = null;
            //首先从cookie中查找购物车
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);

            if (cartListFromCookie != null && cartListFromCookie.size() > 0){
                //合并数据
                cartList = cartService.mergeToCartList(cartListFromCookie, userId);
                //合并完成后删除cookie中的购物车数据
                cartCookieHandler.deleteCartCookie(request, response);
            }else {
                //cookie中没有购物车的信息，从redis或者数据库中获取cartList信息
                cartList = cartService.getCartList(userId);
            }
            request.setAttribute("cartList", cartList);
        }else {
            //用户没有登录，从cookie中取数据
            List<CartInfo> cartList =  cartCookieHandler.getCartList(request);
            request.setAttribute("cartList", cartList);
        }
        return "cartList";
    }

    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request, HttpServletResponse response){
        //修改被勾选的商品信息，重新生成一个购物车，用于记录已经被勾选的商品
        //获取商品和用户信息
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");
        //根据用户是否登录，判断对缓存或者cookie中的数据进行修改
        if (userId != null){
            //用户已经登录，修改redis中的商品信息即可
            cartService.checkCart(skuId, isChecked, userId);
        }else {
            //用户未登录，修改cookie中的商品信息
            cartCookieHandler.checkCart(skuId, isChecked, request, response);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
        if (cookieHandlerCartList != null && cookieHandlerCartList.size() > 0){
            cartService.mergeToCartList(cookieHandlerCartList, userId);
            cartCookieHandler.deleteCartCookie(request, response);
        }
        return "redirect://order.gmall.com/trade";
    }

}
