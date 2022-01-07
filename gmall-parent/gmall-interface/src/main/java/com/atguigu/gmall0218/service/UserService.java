package com.atguigu.gmall0218.service;


import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.UserInfo;
import java.util.List;

public interface UserService {
    /**
     * 查询所有数据
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据userId 查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);


    /**
     * 根据用户输入的登录信息查询是否存在此用户，并返回相关信息
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 验证用户是否已经登录过
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
