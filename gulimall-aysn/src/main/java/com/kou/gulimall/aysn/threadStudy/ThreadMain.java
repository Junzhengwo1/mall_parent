package com.kou.gulimall.aysn.threadStudy;

import java.util.concurrent.*;

public class ThreadMain {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {




        /**
         * 1、继承Thread
         * 2、实现Runnable接口
         * 3、实现Callable接口 + FutureTask
         * 4、线程池 (可以回收线程) TODO 我们应该使用线程池来
         */
        System.out.println("main启动了……");
//        ThreadService01 threadService01 = new ThreadService01();
//        threadService01.start();

//        RunnableService02 runnableService02 = new RunnableService02();
//        new Thread(runnableService02).start();

//        FutureTask<Integer> futureTask = new FutureTask<>(new CallableService());
//        new Thread(futureTask).start();
//        Integer integer = futureTask.get();//等待线程执行完成后拿到返回结果
//        System.out.println(integer);


        /**
         * 线程池的玩法:
         * 线程池的细节：
         * todo：
         * 1、七大参数
         * int corePoolSize, 核心线程数；线程池创建好之后就等待任务；一直存在
         * int maximumPoolSize, 池里面最大线程数；控制资源
         * long keepAliveTime, 存活时间；如果当前的线程数量大于核心数量；释放空闲线程
         * TimeUnit unit, 时间单为
         * BlockingQueue<Runnable> workQueue, 阻塞队列；如果任务有很多的话，会把多的任务放在队列里边；只要有线程空闲就回去去执行
         * ThreadFactory threadFactory, 线程的创建工厂
         * RejectedExecutionHandler handler 如果队列满了，按照我们指定的拒绝策略来拒绝执行任务
         * 2、常见的线程四种线程池
         * Executors.newCachedThreadPool();
         * Executors.newFixedThreadPool();
         * Executors.newScheduledThreadPool();
         * Executors.newSingleThreadScheduledExecutor();
         * 3、
         *
         */
//        //保证项目有一两个线程池
//        service.execute(new RunnableService02());

        System.out.println("main结束……");

        System.out.println("------------------线程池玩法---------------");
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5,
                200,
                10,TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());




    }







}
