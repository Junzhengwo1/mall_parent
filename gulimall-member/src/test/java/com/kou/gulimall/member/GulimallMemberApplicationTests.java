package com.kou.gulimall.member;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.Test;


class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(DigestUtil.md5Hex("123"));
    }

}
