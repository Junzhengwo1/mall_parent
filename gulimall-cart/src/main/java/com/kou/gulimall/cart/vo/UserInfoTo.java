package com.kou.gulimall.cart.vo;


import lombok.Data;

@Data
public class UserInfoTo {


    private String userKey;

    private Long userId;

    private Boolean tempUser = false;

}
