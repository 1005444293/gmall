package com.atguigu.gmall0218.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    //引入jedisPool连接池
    private JedisPool jedisPool;

    /**
     * 初始化jedisPool连接池
     * @param host 连接的服务器地址
     * @param port redis在服务器上的端口号
     * @param database 连接的redis的数据库
     */
    public void initJedisPool(String host, int port, int database){
        //创建初始化jedisPool的配置对象jedisPoolConfig
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置连接池中最大的连接数
        jedisPoolConfig.setMaxTotal(200);
        //获取连接时等待的最大毫秒数10*1000秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        //连接池中最少的剩余数
        jedisPoolConfig.setMinIdle(10);
        //如果到达最大连接数，设置未获取连接的线程等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);
        //
        jedisPool = new JedisPool(jedisPoolConfig, host, port, 20*1000);
    }

    /**
     * 获取jedis连接
     * @return
     */
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
