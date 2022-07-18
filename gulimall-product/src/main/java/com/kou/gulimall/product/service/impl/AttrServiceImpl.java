package com.kou.gulimall.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.kou.gulimall.common.constant.ProductConstant;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.kou.gulimall.product.dao.AttrDao;
import com.kou.gulimall.product.dao.AttrGroupDao;
import com.kou.gulimall.product.dao.CategoryDao;
import com.kou.gulimall.product.entity.*;
import com.kou.gulimall.product.service.AttrService;
import com.kou.gulimall.product.service.CategoryService;
import com.kou.gulimall.product.vo.AttrGroupRelationVo;
import com.kou.gulimall.product.vo.AttrRespVo;
import com.kou.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }



    @Transactional
    @Override
    public void saveAttr(AttrVo vo) {
        //1、保存基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.save(attrEntity);
        //2、保存关联关系
        if( vo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && ObjectUtil.isNotNull(vo.getAttrGroupId())){
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(vo.getAttrGroupId());
        relationEntity.setAttrId(attrEntity.getAttrId());
        relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId,String attrType) {
        LambdaQueryWrapper<AttrEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AttrEntity::getAttrType,"base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if(catelogId != 0 ){
            queryWrapper.eq(AttrEntity::getCatelogId,catelogId);
        }
        if(StrUtil.isNotBlank(key)){
            queryWrapper.and(o->{
               queryWrapper.eq(AttrEntity::getAttrId,key).or().like(AttrEntity::getAttrName,key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> collect = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
            //设置分组
            if("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(wrapper);
                if (ObjectUtil.isNotNull(relationEntity) && ObjectUtil.isNotNull(relationEntity.getAttrGroupId())) {
                    Long attrGroupId = relationEntity.getAttrGroupId();
                    String attrGroupName = attrGroupDao.selectById(attrGroupId).getAttrGroupName();
                    attrRespVo.setGroupName(attrGroupName);
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (ObjectUtil.isNotNull(categoryEntity)) {
                String categoryName = categoryEntity.getName();
                attrRespVo.setCatelogName(categoryName);
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(collect);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);
        //设置分组信息
        if(attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())){
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
            if (ObjectUtil.isNotNull(relationEntity)) {
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if(ObjectUtil.isNotNull(attrGroupEntity)){
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (ObjectUtil.isNotNull(categoryEntity)) {
            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }


    /**
     * 分组信息的更新或者新增
     * @param vo
     */
    @Transactional
    @Override
    public void updateAttr(AttrVo vo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.updateById(attrEntity);
        //修改分组关联
        if(attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(vo.getAttrGroupId());
            relationEntity.setAttrId(vo.getAttrId());
            Integer count = relationDao.selectCount(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
            if(count > 0){
                relationDao.update(relationEntity,new LambdaUpdateWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId,vo.getAttrId()));
            }else {
                relationDao.insert(relationEntity);
            }
        }
    }


    /**
     * 根据分组id找到关联的所有属性 基本属性
     * @param attrGroupId 分组ID
     * @return 对应结果
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrGroupId,attrGroupId);
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(queryWrapper);
        List<AttrEntity> result = Lists.newArrayList();
        if(CollectionUtil.isNotEmpty(relationEntities)){
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            List<AttrEntity> attrEntities = this.listByIds(attrIds);
            result.addAll(attrEntities);
        }
        return result;
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationVo> vos) {

//        LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper=Wrappers.lambdaQuery();
//        queryWrapper.in(AttrAttrgroupRelationEntity::getAttrId,)

        List<AttrAttrgroupRelationEntity> relationEntities = vos.stream().map(vo -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);
    }

    /**
     * 获取当前分组没有关联的属性
     * @param params 分页的对象参数
     * @param attrGroupId 分组ID
     * @return page
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        //1、当前分组只能关联自己所属的分类的属性
        //2、当前分组只能关联别的分组没有引用的分组
        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(params);
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //当前分类的其他分组；以及这些分组关联的属性；然后将这些剔除掉
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AttrGroupEntity::getCatelogId,catelogId);
        //queryWrapper.ne(AttrGroupEntity::getAttrGroupId,attrGroupId);
        //拿到了该类别下的所有分组信息eg:除去主体之外的（基本信息，主芯片)
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(queryWrapper);
        if(CollectionUtil.isNotEmpty(attrGroupEntities)){
            List<Long> otherGroupIds = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = Wrappers.lambdaQuery();
            wrapper.in(AttrAttrgroupRelationEntity::getAttrGroupId,otherGroupIds);
            List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(wrapper);
            if(CollectionUtil.isNotEmpty(relationEntities)){
                List<Long> ortherAttrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                LambdaQueryWrapper<AttrEntity> attWrapper = Wrappers.lambdaQuery();
                attWrapper.eq(AttrEntity::getCatelogId,catelogId);
                attWrapper.eq(AttrEntity::getAttrType,ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
                if(CollectionUtil.isNotEmpty(ortherAttrIds)) {
                    attWrapper.notIn(AttrEntity::getAttrId, ortherAttrIds);
                }
                //查询条件
                String key = (String) params.get("key");
                if(StrUtil.isNotBlank(key)){
                    attWrapper.and(w->{
                        w.eq(AttrEntity::getAttrId,key).or().like(AttrEntity::getAttrName,key);
                    });
                }
                page = this.page(page, attWrapper);
            }
        }
        return new PageUtils(page);
    }




}