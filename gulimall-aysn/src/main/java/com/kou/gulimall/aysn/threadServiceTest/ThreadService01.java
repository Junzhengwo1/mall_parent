package com.kou.gulimall.aysn.threadServiceTest;


/**
 * 去继承Thread
 */
public class ThreadService01 extends Thread {

    //重写run()方法
    @Override
    public void run() {
        System.out.println("当前线程："+Thread.currentThread().getId());
        //对应的业务代码
        int i = 10 / 2;
        System.out.println("result"+ i);


    }
}
