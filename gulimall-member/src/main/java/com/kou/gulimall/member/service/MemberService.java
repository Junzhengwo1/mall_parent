package com.kou.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.member.entity.MemberEntity;
import com.kou.gulimall.member.exception.PhoneExsitException;
import com.kou.gulimall.member.exception.UserNameExsitException;
import com.kou.gulimall.member.vo.UserLoginVo;
import com.kou.gulimall.member.vo.UserRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:23:28
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(UserRegistVo vo);

    void checkUserNameUnique(String userName) throws UserNameExsitException;

    void checkPhoneUnique(String phone) throws PhoneExsitException;

    MemberEntity login(UserLoginVo vo);
}

