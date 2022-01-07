package com.atguigu.gmall0218.manage.mapper;

import com.atguigu.gmall0218.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    /**
     * 制作SKU页面中 根据catalog3Id的值查询包含AttrValue的AttrInfoList数据集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoListByCatalog3Id(String catalog3Id);

    /**
     * 根据attrValueIds中的ids查询商品的平台属性值base_attr_info/base_attr_value
     * @param attrValueIds
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("attrValueIds") String attrValueIds);
}
