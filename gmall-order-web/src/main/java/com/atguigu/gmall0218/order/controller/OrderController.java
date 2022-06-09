package com.atguigu.gmall0218.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.OrderDetail;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.enums.OrderStatus;
import com.atguigu.gmall0218.bean.enums.ProcessStatus;
import com.atguigu.gmall0218.config.LoginRequire;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;


//        @RequestMapping("trade")
//        public String trade(){
//            // 返回一个视图名称叫index.html
//            return "index";
//        }
    // http://localhost:8081?userId=1
//    @RequestMapping("trade")
//    // 第一个返回json 字符串，fastJson.jar
//    // 第二直接将数据显示到页面！
//    @ResponseBody
//    public List<UserAddress> trade(String userId){
//        // 返回一个视图名称叫index.html
//        return userService.getUserAddressList(userId);
//    }

    /**
     * 将被选中的商品信息放到订单详情中并存入作用域展示在前端页面
     * @param request
     * @return
     */
    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String trade(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");
        //获取被选中的商品列表
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        //获取收货人地址
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        //将收货人地址存储到作用域中
        request.setAttribute("userAddressList", userAddressList);
        //声明一个存储商品详情的集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //将cartCheckedList信息放到orderDetailList中
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        //将商品详情放到作用域中
        request.setAttribute("orderDetailList", orderDetailList);
        //订单页面的所有订单信息，包括订单的总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //将订单总金额放到作用域中
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        //获取流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        //获取当前要提交订单的用户
        String userId = (String) request.getAttribute("userId");
        // 检查tradeCode，首先从页面取出trade页面生成的流水号，然后再跟缓存中的流水号比对
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        //初始化订单参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setUserId(userId);
        orderInfo.sumTotalAmount();

        // 校验，验价
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            // 从订单中去购物skuId，数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result) {
                request.setAttribute("errMsg", "商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }
        //将订单保存到数据库，返回生成的订单id，以此避免重复提交
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo
        orderService.delTradeNo(userId);
        //重定向到支付页面
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}