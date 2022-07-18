package com.kou.gulimall.member.controller;

import cn.hutool.core.util.ObjectUtil;
import com.kou.gulimall.common.exception.BizCodeEnum;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.member.entity.MemberEntity;
import com.kou.gulimall.member.exception.PhoneExsitException;
import com.kou.gulimall.member.exception.UserNameExsitException;
import com.kou.gulimall.member.feign.CouponFeignService;
import com.kou.gulimall.member.service.MemberService;
import com.kou.gulimall.member.vo.UserLoginVo;
import com.kou.gulimall.member.vo.UserRegistVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;




/**
 * 会员
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:23:28
 */
@Api(tags = "会员controller")
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    /**
     * 注册功能
     */
    @ApiOperation("会员注册")
    @PostMapping("/regist")
    public R regist(@RequestBody UserRegistVo vo){
        try{
            memberService.regist(vo);
        }catch (UserNameExsitException e){
            return R.error(BizCodeEnum.USER_EXIST_EXPCEPTION.getCode(),e.getMessage());
        }catch (PhoneExsitException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXPCEPTION.getCode(),e.getMessage());
        }
        return R.ok();
    }

    /**
     * 登录功能
     */
    @ApiOperation("会员登录")
    @PostMapping("/Login")
    public R Login(@RequestBody UserLoginVo vo){
        MemberEntity entity=memberService.login(vo);
        if(ObjectUtil.isNotNull(entity)){
            return R.ok().put("data",entity);
        }else {
            return R.error(BizCodeEnum.LOGIN_USERNAME_OR_PASSWORD_WRONG.getCode(),BizCodeEnum.LOGIN_USERNAME_OR_PASSWORD_WRONG.getMsg());
        }

    }








    /**
     * TODO 调用测试
     */
    @RequestMapping("/test/getCoupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("king");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("members",memberEntity)
                .put("memberCoupons",memberCoupons);
    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("data", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
