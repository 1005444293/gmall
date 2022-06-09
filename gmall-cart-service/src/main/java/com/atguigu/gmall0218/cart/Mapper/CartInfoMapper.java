package com.atguigu.gmall0218.cart.Mapper;

import com.atguigu.gmall0218.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * 从CartInfo和SkuInfo两张表查出购物车中cartInfo所需的所有商品信息
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
