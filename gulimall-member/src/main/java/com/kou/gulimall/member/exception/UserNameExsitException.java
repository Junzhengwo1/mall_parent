package com.kou.gulimall.member.exception;

public class UserNameExsitException extends RuntimeException {

    public UserNameExsitException () {
        super("用户名已经存在");
    }
}
