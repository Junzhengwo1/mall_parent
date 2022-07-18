package com.kou.gulimall.ware.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.ware.dao.WareInfoDao;
import com.kou.gulimall.ware.entity.WareInfoEntity;
import com.kou.gulimall.ware.feign.MemberFeignService;
import com.kou.gulimall.ware.service.WareInfoService;
import com.kou.gulimall.ware.vo.FareVo;
import com.kou.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = info.getData("data", new TypeReference<MemberAddressVo>() {});
        if(ObjectUtil.isNotNull(data)){
            //这里我们就那手机号的最后一位数字模拟运费
            String phone = data.getPhone();
            String fare = phone.substring(phone.length() - 1, phone.length());
            fareVo.setMemberAddressVo(data);
            fareVo.setFare(new BigDecimal(fare));
            return fareVo;
        }
        return null;
    }

}