package com.kou.gulimall.member.dao;

import com.kou.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:23:28
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
