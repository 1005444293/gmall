package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.OrderInfo;

public interface OrderService {

    /**
     * 在订单详情页面，将将要提交的订单保存在数据库中
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void  delTradeCode(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 删除流水号
     * @param userId
     */
    void delTradeNo(String userId);
}
