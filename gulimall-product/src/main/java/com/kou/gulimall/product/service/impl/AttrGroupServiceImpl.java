package com.kou.gulimall.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.product.dao.AttrGroupDao;
import com.kou.gulimall.product.entity.AttrEntity;
import com.kou.gulimall.product.entity.AttrGroupEntity;
import com.kou.gulimall.product.service.AttrGroupService;
import com.kou.gulimall.product.service.AttrService;
import com.kou.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.kou.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String)params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(key)) {
            wrapper.and(obj->{
                obj.eq(AttrGroupEntity::getAttrGroupId,key).or().like(AttrGroupEntity::getAttrGroupName,key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }else {
            wrapper.eq(AttrGroupEntity::getCatelogId,catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }

    }

    /**
     * 根据分类id查询所有的分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCateId(Long catelogId) {

        //查询对应的所有分组
        LambdaQueryWrapper<AttrGroupEntity> wrapper= Wrappers.lambdaQuery();
        wrapper.eq(AttrGroupEntity::getCatelogId,catelogId);
        List<AttrGroupEntity> groupEntities = this.list(wrapper);
        //查询分组的所有属性
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(o -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(o, vo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(o.getAttrGroupId());
            vo.setAttrs(attrEntities);
            return vo;
        }).collect(Collectors.toList());
        return collect;

    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId , Long catalogId) {
        List<SpuItemAttrGroupVo> vos = this.getBaseMapper().getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }


}