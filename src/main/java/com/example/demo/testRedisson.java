package com.example.demo;

import jakarta.annotation.Resource;
import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class testRedisson {

    @Autowired
    private RedissonClient redissonClient;

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class);
        RedissonClient redissonClient = context.getBean(RedissonClient.class);

        RLock lock = redissonClient.getLock("redissonLock");
        boolean tryLock = lock.tryLock(20, TimeUnit.SECONDS);
        tryLock
//        方法的核心逻辑：
//        尝试直接获取锁。
//        如果锁被其他线程持有，订阅锁释放事件。
//        在等待时间内，不断尝试获取锁。
//        如果成功获取锁，返回 true；否则返回 false。
        if (tryLock) {
            System.out.println("加锁成功");
        }

    }

    @Test
    public void method1() {
        ApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class);
        RedissonClient redissonClient = context.getBean(RedissonClient.class);
        RLock lock = redissonClient.getLock("redissonLock");
        boolean isLock = lock.tryLock();
        if (!isLock) {
            System.out.println("获取锁失败");
            return;
        }
        try {
            System.out.println("method1获取锁成功");
            method2(lock); // 将锁对象传递给 method2
        } finally {
            lock.unlock();
            System.out.println("method1释放锁");
        }
    }

    void method2(RLock lock) {
        boolean isLock = lock.tryLock();
        if (!isLock) {
            System.out.println("method2获取锁失败");
            return;
        }
        try {
            System.out.println("method2获取锁成功");
        } finally {
            lock.unlock();
            System.out.println("method2释放锁");
        }
    }

}
