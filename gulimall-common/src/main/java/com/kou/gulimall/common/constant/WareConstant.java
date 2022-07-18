package com.kou.gulimall.common.constant;

import lombok.Getter;

public class WareConstant {


    @Getter
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEVICE(2, "已领取"),
        FINISH(3,"已完成"),
        HASEEOR(4,"有异常");

        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }



    @Getter
    public enum PurchaseDetailStatusEnum{

        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2, "正在采购"),
        FINISH(3,"已完成"),
        HASEEOR(4,"采购失败");

        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }


    }

}
