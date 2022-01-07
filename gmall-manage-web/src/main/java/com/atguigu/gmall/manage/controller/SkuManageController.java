package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.bean.SkuLsInfo;
import com.atguigu.gmall0218.bean.SpuImage;
import com.atguigu.gmall0218.service.ListService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){

        return manageService.getSpuImageList(spuId);
    }

    // 上传一个商品，如果上传批量！
    @RequestMapping("onSale")
    @ResponseBody
    public void onSale(String skuId){
        // 众筹属性不能拷贝！？
        // 创建一个skuLsInfo 对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 给skuLsInfo 赋值！
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        System.out.println(skuInfo);
        // 属性拷贝！
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
        System.out.println(skuLsInfo);
    }
}
