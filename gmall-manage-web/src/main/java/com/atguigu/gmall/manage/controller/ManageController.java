package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// 解决与后台管理系统界面数据传输和api调用的跨域问题，后台管理系统的端口号为8888,
@CrossOrigin
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){

        return manageService.getCatalog1();
    }
//    http://localhost:8082/getCatalog1
    // http://localhost:8082/getCatalog2?catalog1Id=1
    // http://localhost:8082/getCatalog2?catalog1Id=2
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){

        return manageService.getCatalog2(catalog1Id);
    }

    // http://localhost:8082/getCatalog3?catalog2Id=37
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){

        return manageService.getCatalog3(catalog2Id);
    }


    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        return manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    //@RequestBody 用于将前端传递来的json数据自动封装成BaseAttrInfo对象
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        //先根据attrId获取baseAttrInfo
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        //再由baseAttrInfo获取baseAttrValue集合
        return baseAttrInfo.getAttrValueList();
    }

    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){

        return manageService.getBaseSaleAttrList();
    }

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }

}
