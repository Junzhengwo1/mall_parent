package com.kou.gulimall.aysn.SelfStudyThread.ListThreadStuduy;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TODO
 * 模拟集合线程不安全的情况
 */
public class ThreadForList {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        //List<String> strings = Collections.synchronizedList(list);//比较古老
        //（写时复制技术）
        List<String> strings = new CopyOnWriteArrayList<>();//线程安全的解决方案
//        for (int i = 1; i <=10 ; i++) {
//            new Thread(()->{
//                strings.add(UUID.randomUUID().toString().substring(0,4));
//                System.out.println(strings);
//            },String.valueOf(i)).start();
//
//        }


        /**
         * 演示HashSet线程不安全问题
         */
//        HashSet<String> set = new HashSet<>();
//        CopyOnWriteArraySet<String> hashSet = new CopyOnWriteArraySet<>();
//        for (int i = 1; i <=10 ; i++) {
//            new Thread(()->{
//                set.add(UUID.randomUUID().toString().substring(0,4));
//                System.out.println(set);
//            },String.valueOf(i)).start();
//
//        }

        /**
         * 演示HashMap线程不安全的问题
         */
        Map<String, String> map = new HashMap<>();
        //解决方案
        Map<String, String> map1 = new ConcurrentHashMap<>();
        for (int i = 1; i <=10 ; i++) {
            String key = String.valueOf(i);
            new Thread(()->{
                map1.put(key,UUID.randomUUID().toString().substring(0,4));
                System.out.println(map1);
            },String.valueOf(i)).start();

        }

    }
}
