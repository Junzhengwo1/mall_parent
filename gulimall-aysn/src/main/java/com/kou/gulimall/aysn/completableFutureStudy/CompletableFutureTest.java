package com.kou.gulimall.aysn.completableFutureStudy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {

    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 解决 任务编排问题
         */
        System.out.println("mainStart");

//        CompletableFuture.runAsync(()->{
//            System.out.println("当前线程："+Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result"+ i);
//        },executorService);

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService);

        /**
         * 方法完成后的感知
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService).whenComplete((res,excption)->{
//            System.out.println("异步任务成功完成了……结果是"+res+";"+"异常是："+excption);
//        }).exceptionally(throwable -> { //能够感知异常同时返回默认值
//            return 10;
//        });

        /**
         * 方法完成后的处理 Handle
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService).handle((res,e)->{
//            if(res != null){
//                return res*2;
//            }
//            if(e!= null){
//                return 5;
//            }
//            return 0;
//        });


        /**
         * 线程串行化方法 thenApply  thenAccept
         * 1\thenRun不能获取上一步执行的结果
         */
//        CompletableFuture<Void> async = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService).thenRunAsync(() -> {
//            System.out.println("任务2启动了……");
//        }, executorService);

        //thenAccept来接受上一步的结果
//        CompletableFuture<Void> async = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService).thenAcceptAsync((res) -> {
//            System.out.println("任务2启动了……"+res);
//
//        }, executorService);

        //thenApply来接受上一步的结果
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            //对应的业务代码
//            int i = 10 / 2;
//            System.out.println("result" + i);
//            return i;
//        }, executorService).thenApplyAsync((res) -> {
//            System.out.println("任务2启动了……" + res);
//            return 500 + res;
//        }, executorService);

        /**
         * 两个任务都完成
         */
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            //对应的业务代码
            int i = 10 / 2;
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            System.out.println("任务一结束" + i);
            return i;
        }, executorService);

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            //对应的业务代码
            int i = 10 / 5;
            System.out.println("任务二结束" + i);
            return i;
        }, executorService);

        //这个不能感知到前两个的结果的
//        future1.runAfterBothAsync(future2,()->{
//            System.out.println("任务三开始");
//        },executorService);

//        future1.thenAcceptBothAsync(future2,(f1,f2)->{
//            System.out.println("任务三开始……之前的结果"+(f1+f2));
//        },executorService);

        CompletableFuture<Integer> future = future1.thenCombineAsync(future2, (f1, f2) -> {
            return f1 + f2;
        }, executorService);


        /**
         * 两个任务组合 一个完成…… 不感知结果
         */
        future1.runAfterEitherAsync(future2,()->{
            System.out.println("任务三执行……");
        },executorService);





        /**
         * 多任务组合
         * allOf()
         * anyOF()
         * 模拟
         */
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello_.jpg";
        }, executorService);

        CompletableFuture<String> futurePro = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色_.jpg";
        }, executorService);

        CompletableFuture<String> futureInr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的介绍");
            return "华为";
        }, executorService);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureInr, futurePro);


        //System.out.println("mainEND……"+future.get());
        System.out.println("mainEND……");

    }
}
