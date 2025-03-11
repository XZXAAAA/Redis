package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String key = "mykey";

    public void tryLock(String uniqueIdentifier) {
        String prefix = "Thread:";
        String value = prefix + uniqueIdentifier;
        Boolean inputKey = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(20));
        if (inputKey) {
            System.out.println("打印成功");
            System.out.println(redisTemplate.opsForValue().get(key));
        } else {
            System.out.println("插入锁失败");
        }
    }

    public void unlock(String uniqueIdentifier) {
        String prefix = "Thread:";
        String value = prefix + uniqueIdentifier;

        // Lua 脚本
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";

        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(key),
                value
        );

        if (result == 1) {
            System.out.println("当前执行删除锁的就是持有锁的线程");
        } else {
            System.out.println("当前删除Redis的key的不是持有锁的线程");
        }
    }
}