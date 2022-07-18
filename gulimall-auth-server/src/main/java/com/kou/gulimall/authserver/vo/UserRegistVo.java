package com.kou.gulimall.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 2,max = 6,message = "用户名必须是2-6字符")
    private String userName;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1][3-9][0-9]{9}$",message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 8,max = 18,message = "密码必须是6-18字符")
    private String passWord;

    @NotEmpty(message = "验证码不能为空")
    private String code;

}
