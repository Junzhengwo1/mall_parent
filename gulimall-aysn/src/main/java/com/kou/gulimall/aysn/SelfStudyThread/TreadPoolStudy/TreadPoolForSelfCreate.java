package com.kou.gulimall.aysn.SelfStudyThread.TreadPoolStudy;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 */
public class TreadPoolForSelfCreate {

    public static void main(String[] args) {

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                2,
                5,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());


        //模拟处理
        try {
            for (int i = 1; i <=10; i++) {
                poolExecutor.execute(()->{
                    System.out.println(Thread.currentThread().getName()+"开始执行");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
