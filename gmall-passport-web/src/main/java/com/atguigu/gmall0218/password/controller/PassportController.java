package com.atguigu.gmall0218.password.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.UserInfo;
import com.atguigu.gmall0218.password.config.JwtUtil;
import com.atguigu.gmall0218.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @Value("${token.key}")
    private String key;

    @Value("${token.salt}")
    private String saltKey;

    @RequestMapping("index")
    public String index(HttpServletRequest request){

        //获取用户点击登录页面的url，用于验证后跳转到此url
        String originUrl = request.getParameter("originUrl");
        //保存url
        request.setAttribute("originUrl", originUrl);
        System.out.println(originUrl);
        return "index";
    }

    /**
     * 用于登录
     * @param userInfo
     * @param request
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        //获取slat盐
//        String salt = request.getHeader("X-forwarded-for");
        String salt = saltKey;
        //如果数据库中有当前用户，将其信息查出并制作token
        if (userInfo != null){
            UserInfo info = userService.login(userInfo);
            if (info != null){
                //生成token
                Map<String, Object> map = new HashMap<>();
                map.put("userId", info.getId());
                map.put("nickName", info.getNickName());
                String token = JwtUtil.encode(key, map, salt);
                System.out.println(token);
                return token;
            }else {
                return "fail";
            }
        }
        return "fail";
    }

    /**
     * 用于验证用户是否已经登录
     * @param request
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
//        1.	获取服务器的Ip，token
//        2.	key+ip ,解密token 得到用户的信息 userId,nickName
//        3.	判断用户是否登录：key=user:userId:info  value=userInfo
//        4.	userInfo!=null true success; false fail;
//        String salt = request.getHeader("X-forwarded-for");
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        // 调用jwt工具类
//        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "1");
        map.put("nickName", "Atguigu");

        if (map!=null && map.size()>0){
            // 获取userId
            String userId = (String) map.get("userId");
            // 调用服务层查询用户是否已经登录
            UserInfo userInfo = userService.verify(userId);
            if (userInfo!=null){
                return "success";
            }else {
                return "fail";
            }
        }
        return "fail";

    }

}
