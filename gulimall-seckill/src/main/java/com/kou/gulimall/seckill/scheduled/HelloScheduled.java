//package com.kou.gulimall.seckill.scheduled;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
///**
// * 定时任务异步执行
// */
//
//@Slf4j
//@Component
//@EnableScheduling
//@EnableAsync
//public class HelloScheduled {
//
//    @Scheduled(cron = "* * * * * ?")
//    @Async
//    public void printHello() throws InterruptedException {
//      log.info("hello__");
//      Thread.sleep(3000);
//    }
//}
