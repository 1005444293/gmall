package com.atguigu.gmall0218.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration 相当于spring3.0版本的xml
@Configuration
public class RedisConfig {

    //读取manage-service配置文件中的redis的地址、端口和数据库等参数
    //disabled：表示未指定host时，host默认为disabled
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.database}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){
        //如果host为disabled，返回为空
        if (host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host, port, database);
        return redisUtil;
    }
}
