package com.kou.gulimall.aysn.threadServiceTest;

import java.util.concurrent.Callable;

/**
 * 实现callable接口的方式
 */
public class CallableService implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {

        System.out.println("实现Callable接口的方式");
    return 20;

    }
}
