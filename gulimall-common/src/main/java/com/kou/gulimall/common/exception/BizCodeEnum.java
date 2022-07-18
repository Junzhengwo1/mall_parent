package com.kou.gulimall.common.exception;


public enum  BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知错误"),
    VAILD_EXCEPTION(10001,"参数检验失败"),
    SMS_CODE_EXPCEPTION(10002,"短信验证码频率太高；稍后再试"),
    REGIST_EXPCEPTION(10003,"注册失败，请检查注册信息"),
    LOGIN_EXPCEPTION(10004,"登录失败，请检查登录信息"),
    PRODUCT_UP_EXPCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXPCEPTION(15000,"用户名已存在"),
    PHONE_EXIST_EXPCEPTION(15001,"手机号存在"),
    LOGIN_USERNAME_OR_PASSWORD_WRONG(15003,"账号或密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    ORDER_TO_BUY_ERROR(30000,"下单失败，请检查下单商品信息");

    private int code;

    private String msg;


    BizCodeEnum(int code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
