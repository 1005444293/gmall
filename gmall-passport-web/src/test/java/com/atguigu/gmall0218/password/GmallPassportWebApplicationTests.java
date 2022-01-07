package com.atguigu.gmall0218.password;


import com.atguigu.gmall0218.password.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testJwt(){
        String key = "atguigu";
        String ip = "192.168.61.128";
        Map map = new HashMap<>();
        map.put("userId", "1001");
        map.put("nickName", "marry");
        String token = JwtUtil.encode(key, map, ip);
        Map<String, Object> deToken = JwtUtil.decode(token, key, ip);
        System.out.println(token);
        System.out.println(deToken);
    }

}
