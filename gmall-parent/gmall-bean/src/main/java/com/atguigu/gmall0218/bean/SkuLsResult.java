package com.atguigu.gmall0218.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuLsResult implements Serializable {
    /**
     * 用于返回从es搜索的结果
     * skuLsInfoList：表示sku属性值列表
     * attrValueIdList：平台属性值id(base_attr_value的id)
     */
    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;
}
