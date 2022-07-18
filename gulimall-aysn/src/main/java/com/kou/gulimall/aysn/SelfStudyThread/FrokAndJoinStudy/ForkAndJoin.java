package com.kou.gulimall.aysn.SelfStudyThread.FrokAndJoinStudy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * 分支合并框架
 */

class MyTask extends RecursiveTask<Integer>{

    private static  final Integer VAl = 10;

    private int begin;
    private int end;

    private int result;

    public MyTask (int begin,int end){
        this.begin=begin;
        this.end=end;
    }


    /**
     * 拆分合并逻辑处理
     * @return
     */
    @Override
    protected Integer compute() {
        //判断相加两个数的值是否大于10
        if((end-begin)<=VAl){
            //相加操作
            for (int i = begin; i <=end ; i++) {
                result=result+i;
            }
        }else {
            //拆分操作
            //获取到值的中间值
            int mid = (begin+end)/2;
            MyTask myTask1 = new MyTask(begin, mid);
            MyTask myTask2 = new MyTask(mid + 1, end);
            myTask1.fork();
            myTask2.fork();
            //合并操作
            result = myTask1.join()+myTask2.join();

        }
        return result;
    }
}


public class ForkAndJoin {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        MyTask myTask = new MyTask(0, 100);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<Integer> submit = forkJoinPool.submit(myTask);
        System.out.println(submit.get());
        forkJoinPool.shutdown();

    }

}
