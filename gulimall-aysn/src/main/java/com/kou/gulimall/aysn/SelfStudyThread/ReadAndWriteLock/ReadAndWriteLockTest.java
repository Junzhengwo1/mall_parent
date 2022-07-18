package com.kou.gulimall.aysn.SelfStudyThread.ReadAndWriteLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class MyCache{

    private volatile Map<String,Object> map = new HashMap<>();

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    //向map中放数据
    public void putData(String key,Object val){
        rwlock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+"正在写操作"+key);
            try {
                TimeUnit.MICROSECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Thread.sleep(TimeUnit.SECONDS.toSeconds(2));
            map.put(key,val);
            System.out.println(Thread.currentThread().getName()+"写完了"+key);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            rwlock.writeLock().unlock();
        }

    }

    //向map中取数据

    public Object getData(String key){
        rwlock.readLock().lock();
        Object result = null;
        try {
            System.out.println(Thread.currentThread().getName()+"正在读操作"+key);
            try {
                TimeUnit.MICROSECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result=map.get(key);
            System.out.println(Thread.currentThread().getName()+"正在读完了"+key);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            rwlock.readLock().unlock();
        }
        return result;
    }
}

/**
 * 读写锁案例 模拟
 */
public class ReadAndWriteLockTest {

    public static void main(String[] args) {
        MyCache myCache = new MyCache();

        for (int i = 1; i <=5 ; i++) {
            final int num = i;
            new Thread(()->{
                myCache.putData(String.valueOf(num),num);
            },String.valueOf(i)).start();
        }

        for (int i = 1; i <=5 ; i++) {
            final int num = i;
            new Thread(()->{
                myCache.getData(String.valueOf(num));
            },String.valueOf(i)).start();
        }

    }



}
