package com.atguigu.gmall0218.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.UserInfo;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.UserService;

import com.atguigu.gmall0218.user.mapper.UserAddressMapper;
import com.atguigu.gmall0218.user.mapper.UserInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    //用户信息存入缓存键的前后缀
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> findAll() {

        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        // 调用mapper
        // select * from userAddress where userId=?
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return  userAddressMapper.select(userAddress);

    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //根据当前输入的用户信息，到数据库查询是否有当前用户
        //将用户信息存入缓存中
        String passwd = userInfo.getPasswd();
        //将密码进行加密,MD5加密
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //将加密后的密码存入userinfo中
        userInfo.setPasswd(newPasswd);
        //查询当前用户的信息
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info != null){
            Jedis jedis = null;
            try {
                //将用户信息放入redis中
                jedis = redisUtil.getJedis();
                String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
                jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(info));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭jedis连接
                if (jedis != null){
                    jedis.close();
                }
            }
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis= null;
        try {
            // 获取jedis
            jedis = redisUtil.getJedis();
            // 定义key
            String userKey = userKey_prefix+userId+userinfoKey_suffix;

            String userJson = jedis.get(userKey);
            if (!StringUtils.isEmpty(userJson)){
                // userJson 转成对象
                UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
                return userInfo;

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return null;
    }
}
