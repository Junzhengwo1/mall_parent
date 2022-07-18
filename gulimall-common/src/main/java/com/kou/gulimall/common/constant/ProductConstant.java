package com.kou.gulimall.common.constant;


import lombok.Getter;

public class ProductConstant {

    @Getter
    public enum AttrEnum{

        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性"),

        SEARCHTYPE(1,"属于检索属性");



        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }


    }

    @Getter
    public enum StatusEnum{

        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),

        SPU_DOWN(2,"商品下架");



        private int code;
        private String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }


    }

}
