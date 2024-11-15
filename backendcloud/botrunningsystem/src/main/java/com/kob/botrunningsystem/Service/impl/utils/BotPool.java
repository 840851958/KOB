package com.kob.botrunningsystem.Service.impl.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread{
    private final ReentrantLock lock = new ReentrantLock();
    @Setter
    @Getter
    private Condition condition = lock.newCondition();
    private Queue<Bot> bots = new LinkedList<>(); // 消息队列

    public void addBot(Integer userId, String botCode, String input){
        lock.lock(); // 当bots为空时，run会阻塞线程，直至运行到这个bots.add，通过signal解锁,达成了生产者消费者模型，生产者会不断发送任务，当消费者接收到后开始工作。完成后待机
        try {
            bots.add(new Bot(userId,botCode,input));
            condition.signalAll(); //
        }finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot){ // 后续可以考虑采用docker优化
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);

    }


    @Override
    public void run() { // 如果队列为空，阻塞线程，一旦有新的任务进来会被唤醒
        System.out.println("BotPool 线程已启动。"); // 添加启动日志
        while(true){
            lock.lock();
            if (bots.isEmpty()){
                try {
                    condition.await(); // await阻塞线程，直到被唤醒,并且自动包含了锁释放的过程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else {
                Bot bot = bots.remove(); // remove是返回并删除对头
                lock.unlock();
                consume(bot); // 编译执行代码。比较耗时，可能执行几秒
            }
        }
    }

}
