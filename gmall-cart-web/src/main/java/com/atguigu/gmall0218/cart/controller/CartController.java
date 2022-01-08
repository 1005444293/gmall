package com.atguigu.gmall0218.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.service.CartService;
import org.springframework.stereotype.Controller;

@Controller
public class CartController {

    @Reference
    private CartService cartService;



}
