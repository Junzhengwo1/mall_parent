package com.kou.gulimall.authserver.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "社交登录相干 | 暂未实现")
@RestController
public class OAth2Controller {

    /**
     * todo 社交登录应用场景 应多次回顾-（2021、11、27 暂未实现）
     * todo 分布式 session 后去解决重定向问题后期解决-》redis 的又一应用场景
     * :原理：
     *      1、session同步方案->大型集群不推荐使用
     *      2、让客户端自己存数据，服务器端不存（让浏览器存在cookie里边）->不安全，且数据量小；也是不推荐使用的
     *      3、hash 一致性：
     *      4、session 统一存储： 可以存在redis 等方式
     * todo spring session 应用场景-->index-谷粒商城（225-228）
     *
     * 社交登录、、单点登录
     */
}
