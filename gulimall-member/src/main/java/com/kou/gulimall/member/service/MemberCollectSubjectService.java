package com.kou.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author k.jj
 * @email ${email}
 * @date 2021-09-23 10:23:28
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

