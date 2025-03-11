package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class tryRedisUtil {

    @Autowired
    private RedisUtil redisUtil; // 改为实例变量

    public static void main(String[] args) {
        // 通过 Spring 上下文获取 Bean
        ApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class);
        RedisUtil redisUtil = context.getBean(RedisUtil.class);

        Thread RedisThread = new Thread(() -> {
            long id = Thread.currentThread().getId();
            redisUtil.tryLock(String.valueOf(id));
            try {
                Thread RedisThread2 = new Thread(() -> {
                    long id1 = Thread.currentThread().getId();
                    redisUtil.tryLock(String.valueOf(id1));
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    redisUtil.unlock(String.valueOf(id1));
                });
                RedisThread2.start();
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            redisUtil.unlock(String.valueOf(id));
        });

        RedisThread.start();
    }


}

//当前程序模拟的就是一个线程获取了Redis的一个锁并且进行了休眠
//而在该线程休眠之前，它有调用了另外一个线程。该线程也会尝试进行获取锁和删除锁
//要注意的是，在第一个线程苏醒之前，线程2就尝试删除Redis数据库里面的锁
//因为该锁是线程1创建的，所以线程2无法删除锁。只能等待该锁被线程1主动释放或者到了过期时间被删除

//该代码只是解决了判断删除的key是否属于当前线程所持有。
//但是在后面执行删除Redis key的过程中仍然具有风险
//1、GC停顿。在进行垃圾回收的时，Java虚拟机会暂停所有的应用线程的执行。以便安全的回收不再使用的内存。而就在
//这个时间间隔里面，如果发生了阻塞，其他线程就会在这个时间段里面获取Redis Key，而当阻塞时间过去以后，Redis key
//就会被之前持有的线程造成误删
//对于高并发、低延迟的系统而言，GC停顿可能会导致严重的性能问题

//最好的解决方法就是确保查询锁的持有和执行删除操作的原子性