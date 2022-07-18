package com.kou.gulimall.member.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.member.dao.MemberDao;
import com.kou.gulimall.member.dao.MemberLevelDao;
import com.kou.gulimall.member.entity.MemberEntity;
import com.kou.gulimall.member.entity.MemberLevelEntity;
import com.kou.gulimall.member.exception.PhoneExsitException;
import com.kou.gulimall.member.exception.UserNameExsitException;
import com.kou.gulimall.member.service.MemberService;
import com.kou.gulimall.member.vo.UserLoginVo;
import com.kou.gulimall.member.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(UserRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.queryDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());
        //todo 异常感知 检查用户名和电话的唯一性;(场景使用--为了使controller感知异常；这里使用异常机制)
        this.checkPhoneUnique(vo.getPhone());
        this.checkUserNameUnique(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getNickName());
        //加密处理
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassWord());
        memberEntity.setPassword(encode);
        memberEntity.setMobile(vo.getPhone());
        this.save(memberEntity);
    }


    @Override
    public void checkUserNameUnique(String userName) throws UserNameExsitException {
        int count = this.count(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName));
        if(count>0){
            throw new UserNameExsitException();
        }

    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExsitException {
        int count = this.count(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if(count>0){
            throw new PhoneExsitException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        LambdaQueryWrapper<MemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberEntity::getUsername,vo.getLoginAccount())
                .eq(MemberEntity::getMobile,vo.getPhone());
        MemberEntity one = this.getOne(wrapper);
        if(ObjectUtil.isNull(one)){
            return null;
        }else {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            //密码匹配
            boolean b = encoder.matches(vo.getPassWord(), one.getPassword());
            if(b) {
                return one;
            }else {
                return null;
            }
        }

    }

}