package com.donkey.security.jwt;

import com.donkey.util.redis.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisUtilTest {
    @Autowired
    RedisUtil redisUtil;

    @Test
    public void stringRedis() throws Exception {
        // given
        String key = "string_redis";
        String value = "string";
        // when
        redisUtil.setDataExpire(key, value, 200 * 2L);
        // then
        System.out.println("Redis Key = " + key);
        System.out.println("Redis value = " + redisUtil.getData(key));
    }
}