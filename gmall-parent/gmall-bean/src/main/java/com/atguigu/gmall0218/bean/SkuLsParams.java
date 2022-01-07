package com.atguigu.gmall0218.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuLsParams implements Serializable {
    /**
     * 根据商城首页用户查询商品时可能输入的商品信息，制作当前类，将用户可能输入的商品信息封装成skuLsParams对象
     * 只需又前端输入该对象，根据对象中包含的内容来对商品进行查找和展示
     */
    String keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo = 1;

    int pageSize = 20;
}
