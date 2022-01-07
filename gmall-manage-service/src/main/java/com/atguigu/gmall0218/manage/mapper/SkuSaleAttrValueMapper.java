package com.atguigu.gmall0218.manage.mapper;

import com.atguigu.gmall0218.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    /**
     * 组合skuid相同的sale_attr_value_id，完成选择参数的相应跳转功能
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
