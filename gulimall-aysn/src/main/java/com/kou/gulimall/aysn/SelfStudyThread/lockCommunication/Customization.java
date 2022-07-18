package com.kou.gulimall.aysn.SelfStudyThread.lockCommunication;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ShareResouce{

    private Integer flag = 1;
    private Lock lock = new ReentrantLock();
    private Condition c1 = lock.newCondition();
    private Condition c2 = lock.newCondition();
    private Condition c3 = lock.newCondition();



    //打印5次
    public  void print5(Integer loop)  {
        lock.lock();
        try {
            while (flag != 1){ //这个地方得使用while 否则会出现虚假唤醒问题
                c1.await();
            }
            for (int i=1; i<=5; i++) {
                System.out.println(Thread.currentThread().getName()+":"+"result"+i+":"+loop);
            }
            flag=2;
            c2.signal(); //通知其他线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    //打印10次
    public  void print10(Integer loop)  {
        lock.lock();
        try {
            while (flag != 2){ //这个地方得使用while 否则会出现虚假唤醒问题
                c2.await();
            }
            for (int i=1; i<=10; i++) {
                System.out.println(Thread.currentThread().getName()+":"+"result"+i+":"+loop);
            }
            flag=3;
            c3.signal(); //通知其他线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    //打印15次
    public  void print15(Integer loop)  {
        lock.lock();
        try {
            while (flag != 3){ //这个地方得使用while 否则会出现虚假唤醒问题
                c3.await();
            }
            for (int i=1; i<=15; i++) {
                System.out.println(Thread.currentThread().getName()+":"+"result"+i+":"+loop);
            }
            flag=1;
            c1.signal(); //通知其他线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}

/**
 * 定制化线程通信的 方案 就是立flag
 */
public class Customization{

    public static void main(String[] args) {

        ShareResouce shareResouce = new ShareResouce();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                shareResouce.print5(i);
            }
        },"AA").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                shareResouce.print10(i);
            }
        },"BB").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                shareResouce.print15(i);
            }
        },"CC").start();

    }

}