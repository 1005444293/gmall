package com.atguigu.gmall0218.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0218.bean.OrderDetail;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0218.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 将用户要提交的订单保存到数据库中
     * @param orderInfo
     * @return
     */
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置订单生成时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号，避免重复提交订单
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //保存orderInfo
        orderInfoMapper.insertSelective(orderInfo);

        //将详细的订单信息插入到数据库中
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //绑定orderDetail和orderInfo
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //支付时根据订单id进行支付
        String orderId = orderInfo.getId();
        return orderId;
    }

    // 生成流水号
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    // 验证流水号
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }
    // 删除流水号
    public void  delTradeCode(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        //dubbo只解决控制层远程调用服务层，而控制层调用控制层，可以使用HttpClientUtil解决
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    @Override
    public void delTradeNo(String userId) {
        //定义key
        String tradeNoKey = "user:" + userId + "tradeCode";
        //获取redis
        Jedis jedis = redisUtil.getJedis();
        //删除
        jedis.del(tradeNoKey);
        jedis.close();
    }


}
