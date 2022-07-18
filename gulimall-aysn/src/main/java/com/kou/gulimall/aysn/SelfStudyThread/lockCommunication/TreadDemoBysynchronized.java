package com.kou.gulimall.aysn.SelfStudyThread.lockCommunication;

/**
 * 1、创建资源类
 */

class Share1{

    private Integer number = 0;


    //加一方法
    public synchronized void incr() throws InterruptedException {
        if(number != 0){ //这个地方得使用while 否则会出现虚假唤醒问题
            this.wait();
        }
        number++;
        System.out.println(Thread.currentThread().getName()+":"+"result"+number);
        this.notifyAll(); //通知其他线程
    }

    //减一操作
    public synchronized void decr() throws InterruptedException {
        if(number != 1){
            this.wait();
        }
        number--;
        System.out.println(Thread.currentThread().getName()+":"+"result"+number);
        this.notifyAll(); //通知其他线程
    }

}


class TreadDemoSynchronized {
    public static void main(String[] args) {
        Share1 share1 = new Share1();
        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                try {
                    share1.incr();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"AA").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                try {
                    share1.decr();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"BB").start();
    }
}
