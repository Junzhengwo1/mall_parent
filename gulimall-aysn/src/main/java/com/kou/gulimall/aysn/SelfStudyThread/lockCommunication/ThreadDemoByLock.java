package com.kou.gulimall.aysn.SelfStudyThread.lockCommunication;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Share{

    private Integer number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();



    //加一方法
    public  void incr()  {
        lock.lock();
        try {
            while (number != 0){ //这个地方得使用while 否则会出现虚假唤醒问题
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName()+":"+"result"+number);
            condition.signalAll(); //通知其他线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    //减一操作
    public  void decr() {
        lock.lock();
        try {
            while (number != 1){
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName()+":"+"result"+number);
            condition.signalAll(); //通知其他线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

}

public class ThreadDemoByLock {
    public static void main(String[] args) {
        Share share = new Share();
        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                share.incr();
            }
        },"AA").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                share.decr();
            }
        },"BB").start();
    }
}



