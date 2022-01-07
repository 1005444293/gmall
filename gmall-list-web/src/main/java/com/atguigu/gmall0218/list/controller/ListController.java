package com.atguigu.gmall0218.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.ListService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;
    // http://list.gmall.com/list.html?catalog3Id=61
//    @RequestMapping("list.html")
//    @ResponseBody
//    public String listData(SkuLsParams skuLsParams){
//
//        SkuLsResult skuLsResult = listService.search(skuLsParams);
//
//        return JSON.toJSONString(skuLsResult);
//    }

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model){
        // 设置每页显示的条数
        skuLsParams.setPageSize(2);
        //根据参数返回sku列表
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        //将获取的结果存到作用域中
        model.addAttribute("skuLsResult", skuLsResult.getSkuLsInfoList());

        //获取sku属性值列表
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //获取平台属性值列表
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //获取平台属性值集合
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        //根据用户对平台属性值列表的选择，例如选择收集内存为8G，更新url中和相应产品参数的平台属性值的显示
        String urlParam = makeUrlParam(skuLsParams);

        //定义一个面包屑集合，制作面包屑
        List<BaseAttrValue> baseAttrValueList = new ArrayList<>();
        //对attrList的属性和属性值进行遍历
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            //平台属性
            BaseAttrInfo baseAttrInfo = iterator.next();
            //获取平台属性值集合对象
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //对当前attrValueList进行循环，判断后台查询出的平台属性值集合中是否包含skuLsParams中的平台属性值
            //如果包含就将当前的平台属性值集合中的相应数据移除，不在商品的相应参数位置显示参数，例如8G、128G等参数的显示
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //获取skuLsParams中的平台属性值id
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                    for (String valueId : skuLsParams.getValueId()) {
                        if (valueId.equals(baseAttrValue.getId())){
                            //如果平台属性值id相同，则移除当前数据
                            iterator.remove();
                            // 面包屑组成
                            // baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName();
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            // 将平台属性值的名称改为了面包屑
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            // 将用户点击的平台属性值Id 传递到makeUrlParam 方法中，重新制作返回的url 参数！
                            String newUrlparam = makeUrlParam(skuLsParams, valueId);
                            // 重新制作返回的url 参数！
                            baseAttrValueed.setUrlParam(newUrlparam);
                            // 将baseAttrValueed 放入集合中
                            baseAttrValueList.add(baseAttrValueed);
                        }
                    }
                }
            }

        }

        model.addAttribute("attrList", attrList);

        model.addAttribute("urlParam", urlParam);

        model.addAttribute("keyword", skuLsParams.getKeyword());

        //将sku属性值存入作用域中
        model.addAttribute("skuLsInfoList", skuLsInfoList);

        model.addAttribute("baseAttrValueList", baseAttrValueList);

        model.addAttribute("totalPages", skuLsResult.getTotalPages());

        model.addAttribute("pageNo",skuLsParams.getPageNo());

        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams, String... excludeValueIds) {
        //定义返回值，即拼接后的新的url
        String urlParam = "";
        //SkuLsParams中包含有用户选择商品的途径，搜索栏输入关键字或者catalog3Id...
        //通过对SkuLsParams中包含的信息进行判断，即可对urlParam进行拼接
        //如果用户通过搜索栏进行搜索，那么SkuLsParams中的keyWord应该由内容
        if (skuLsParams.getKeyword() != null){
            urlParam += "keyWord=" + skuLsParams.getKeyword();
        }
        //如果用户通过侧边栏进行搜索，那么catalog3Id应该有值
        if (skuLsParams.getCatalog3Id() != null){
            //判断当前urlParam是否已经包含有搜索条件
            if (urlParam.length() > 0){
                urlParam += "&";
            }
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        // 平台属性值id
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            // 循环遍历
            // http://list.gmall.com/list.html?keyword=手机&catalog3Id=61&valueId=13&valueId=83
            for (String valueId : skuLsParams.getValueId()) {
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 获取点击面包屑时的平台属性值Id
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // break;
                        // continue;
                        continue;
                    }
                }
                // 如果有多个参数则拼接&符号
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }
}
