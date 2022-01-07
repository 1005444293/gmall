package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.SkuLsInfo;
import com.atguigu.gmall0218.bean.SkuLsParams;
import com.atguigu.gmall0218.bean.SkuLsResult;

public interface ListService {

    /**
     * 保存skuLsInfo的数据到es中
     * @param skuLsInfo
     */
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的条件对商品进行查询
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 根据热度对商品进行排序
     * @param skuId
     */
    public void incrHotScore(String skuId);
}
