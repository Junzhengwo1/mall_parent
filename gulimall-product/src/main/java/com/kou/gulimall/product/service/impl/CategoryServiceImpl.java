package com.kou.gulimall.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.product.dao.CategoryDao;
import com.kou.gulimall.product.entity.CategoryEntity;
import com.kou.gulimall.product.service.CategoryBrandRelationService;
import com.kou.gulimall.product.service.CategoryService;
import com.kou.gulimall.product.vo.CateLog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    @Qualifier("redisTemplateMy")
    private RedisTemplate<String,Object> redisTemplateMy;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 三级树形结构展示
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = this.list();
        //组装父子结构
        List<CategoryEntity> levelMenus = entities.stream()
                .filter(e -> e.getParentCid() == 0)
                .map(menu->{
                    menu.setChildren(this.getChildrens(menu,entities));
                    return menu;
                })
                .sorted((menu1,menu2)->{
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) -(menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());

        return levelMenus;
    }

    /**
     * 批量删除
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {

        //TODO 检查菜单是否被引用了

        this.removeByIds(asList);


    }

    //最终效果为 eg:[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = Lists.newArrayList();
        List<Long> parentPath = this.findParentPath(catelogId, paths);
        Collections.reverse(parentPath); //逆序
        log.info("完整路径------->{}",parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }


    /**
     * 级联更新所有关联的数据
     * @CacheEvict:失效模式：
     * @param category
     */
    @Caching(evict = {
            @CacheEvict(value = "categroys",key = "'getLevelOneCategroys'"), //意思就是当我们修改时，redis就会将对应的额缓存数据删除掉|失效模式
            @CacheEvict(value = "categoryJson",key = "'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    //查询所有的一级分类

    /**
     * @return
     */
    @Cacheable(value = {"categroys"},key ="#root.method.name" )
    @Override
    public List<CategoryEntity> getLevelOneCategroys() {
        LambdaQueryWrapper<CategoryEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CategoryEntity::getParentCid, BigDecimal.ZERO.longValue());
        List<CategoryEntity> list = this.list(wrapper);
        return list;
    }


    /**
     * todo 缓存
     * springCache 对缓存出现问题的处理：
     * 缓存失效：
     *      *  todo 缓存穿透  缓存没有命中的 话，就全部跑去查数据库了
     *      *  【查询一个一定不存在的数据】
     *      *  （解决方法，就是将null数据也存起来；并加过期时间）
     *      *  todo 雪崩  查询的数据的
     *      *  key 过期时间都一起失效了导致同一时间全部失效了，请求又全部转发到DB
     *      *  (给失效时间加随机值)
     *      *  todo 缓存击穿
     *      *  热点key
     *      *  （加锁，并发只让一个一个的 查，其他等待）
     * 1）读模式
         * 1、缓存穿透：能够缓存空数据
         * 2、缓存击穿：加锁处理（springCache 默认没有加锁）sync=true 可以解决
         * 3、缓存雪崩：加上随机时间
     * 2）todo 写模式 （保证缓存与数据一致）
     *     1、读写锁
     *     2、引入canal 中间件
     *     3、要不然就干脆直接去查数据库
     * todo 缓存与数据库数据的一致性问题
     *      * 1、双写模式
     *      (改数据库的时候就去改缓存)
     *      * 2、失效模式
     *      只要修改数据库 就删掉缓存  采用分布式读写锁
     *  todo:总结：常规数据（读写小，一致性要求不高的数据）：完全可以使用springCache；特殊数据：就得特殊处理。
     * @return
     */


    /**
     * todo 使用springCahace来实现缓存
     *      *  1、
     *      *  2、
     *      *  3、原理与不足
     *          原理：缓存管理器 负责缓存操作；去获取key 的时候加了同步锁 就已经能有效解决问题了。
     *
     *      *
     * @return
     */
    @Override
    @SuppressWarnings("all")
    @Cacheable(value = "categoryJson",key = "#root.method.name")
    public Map<String, List<CateLog2Vo>> getCatalogJson() {
        /**
         * 1、空结果缓存
         * 2、过期时间
         * 3、加锁
         */

        //继续操作
        List<CategoryEntity> categoryEntityList = this.list();
        //一级分类
        List<CategoryEntity> levelOneCategroys = this.getParentCategories(categoryEntityList, BigDecimal.ZERO.longValue());
        Map<String, List<CateLog2Vo>> map = levelOneCategroys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> { // Map 的 Val
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> list = this.getParentCategories(categoryEntityList, v.getCatId());
            List<CateLog2Vo> cateLog2Vos = null;
            if (CollectionUtil.isNotEmpty(list)) {
                cateLog2Vos = list.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找到当前二级分类的三级分类封装成vo
                    List<CategoryEntity> categoryEntities = this.getParentCategories(categoryEntityList, l2.getCatId());
                    if (CollectionUtil.isNotEmpty(categoryEntities)) {
                        List<CateLog2Vo.Catelog3Vo> catelog3Vos = categoryEntities.stream().map(l3 -> {
                            //2、封装成指定格式
                            CateLog2Vo.Catelog3Vo catelog3Vo = new CateLog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        cateLog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return cateLog2Vo;
                }).collect(Collectors.toList());
            }
            return cateLog2Vos;
        }));

        return map;
    }










    /**
     * todo 缓存问题
     *
     * 优化 三级菜单 | user redis
     * @return result map
     * todo 可能会产生堆溢出 如果是用lettuce去操作redis的话
     * todo 解决方式1、升级lettuce 2、切换使用Jedis
     *
     * 缓存失效：
     *  todo 缓存穿透  缓存没有命中的 话，就全部跑去查数据库了
     *  【查询一个一定不存在的数据】
     *  （解决方法，就是将null数据也存起来；并加过期时间）
     *  todo 雪崩  查询的数据的
     *  key 过期时间都一起失效了导致同一时间全部失效了，请求又全部转发到DB
     *  (给失效时间加随机值)
     *  todo 缓存击穿
     *  热点key
     *  （加锁，并发只让一个一个的 查，其他等待）
     */
    @SuppressWarnings("All")
    public Map<String, List<CateLog2Vo>> getCatalogJsonOLd() {
        /**
         * 1、空结果缓存
         * 2、过期时间加随机值
         * 3、加锁
         */

        //加入缓存逻辑
        Object catalogJson = redisTemplateMy.opsForValue().get("catalogJson");// 可能就会出现缓存穿透
        if(ObjectUtil.isEmpty(catalogJson)){
            assert catalogJson != null;
            //就去数据库拿数据
            Map<String, List<CateLog2Vo>> catalogJsonFromDb = this.getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;

        }
        assert catalogJson != null;
        Map<String, List<CateLog2Vo>> result= JSON.parseObject(JSON.toJSONString(catalogJson),new TypeReference<Map<String, List<CateLog2Vo>>>(){});
        return result;
    }


    /**
     * 分布式锁 | 核心代码 | Redis
     * 核心就是 加锁时保证原子型，解锁时也保证原子性。
     * todo 使用Redisson 来实现分布式锁【1、看门狗机制 2、及时不手动释放锁，也会自动释放锁】
     *
     * todo A调用B的 b在看到a的锁就直接往下执行  也就是嵌套调用不用管
     * todo 缓存与数据库数据的一致性问题
     * 1、双写模式
     * 2、失效模式  只要修改数据库 就删掉缓存  采用分布式读写锁
     * @return result
     */
    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //锁的名字随便写
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock(10,TimeUnit.SECONDS);//阻塞等待
        Map<String, List<CateLog2Vo>> dataFromDb = null;
        try {
            dataFromDb = this.getDataFromDb();
        } finally {
            if(lock.isLocked()){
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }

        }

        return dataFromDb;
    }


    /**
     * 分布式锁 | 核心代码 | Redis
     * todo 核心就是 加锁过期 时间 保证原子型，解锁时也保证原子性 。
     * @return result
     */
    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //todo 1、占分布式锁 ；去redis占锁(并且设置过期时间，防止死锁)
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplateMy.opsForValue().setIfAbsent("lock", uuid,300,TimeUnit.SECONDS);
        if(lock){
            //加锁成功
            //todo 设置过期时间必须与加锁时同步的，防止死锁
            //redisTemplateMy.expire("lock",30,TimeUnit.SECONDS);
            // 下面可能出现死锁问题，（当this.getDataFromDb()方法出现异常 则会造成死锁）
            Map<String, List<CateLog2Vo>> dataFromDb = null;
            try {
                dataFromDb = this.getDataFromDb();
            } finally {
                //删除锁 （为保证不删除另外线程的锁 匹配UUID）|注意也得时原子操作
//            String uuidVal = redisTemplateMy.opsForValue().get("lock").toString();
//            if(uuid.equals(uuidVal)){
//                redisTemplateMy.delete("lock");
//            }
                //使用redis 官方脚本解锁
                String script= "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                Long aLong = redisTemplateMy.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
                log.info("解锁信息{}",aLong);
            }

            return dataFromDb;
        }else {
            //加锁失败……重试
            //休眠一百毫秒执行
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return this.getCatalogJsonFromDbWithRedisLock(); //自旋方式
        }



    }



    /**
     * todo 查数据并放入缓存要放在同一把锁里面 保证原子性
     * @return
     */
    private Map<String, List<CateLog2Vo>> getDataFromDb() {
        //todo 加锁以后，我们应该再去缓存中确定一次，如果没有值的话再继续查询
        Object catalogJson = redisTemplateMy.opsForValue().get("catalogJson");
        if (ObjectUtil.isNotNull(catalogJson)) {
            //缓存不为空直接返回
            Map<String, List<CateLog2Vo>> result = JSON.parseObject(JSON.toJSONString(catalogJson), new TypeReference<Map<String, List<CateLog2Vo>>>() {
            });
            return result;
        }

        //继续操作
        List<CategoryEntity> categoryEntityList = this.list();
        //一级分类
        List<CategoryEntity> levelOneCategroys = this.getParentCategories(categoryEntityList, BigDecimal.ZERO.longValue());
        Map<String, List<CateLog2Vo>> map = levelOneCategroys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> { // Map 的 Val
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> list = this.getParentCategories(categoryEntityList, v.getCatId());
            List<CateLog2Vo> cateLog2Vos = null;
            if (CollectionUtil.isNotEmpty(list)) {
                cateLog2Vos = list.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找到当前二级分类的三级分类封装成vo
                    List<CategoryEntity> categoryEntities = this.getParentCategories(categoryEntityList, l2.getCatId());
                    if (CollectionUtil.isNotEmpty(categoryEntities)) {
                        List<CateLog2Vo.Catelog3Vo> catelog3Vos = categoryEntities.stream().map(l3 -> {
                            //2、封装成指定格式
                            CateLog2Vo.Catelog3Vo catelog3Vo = new CateLog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        cateLog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return cateLog2Vo;
                }).collect(Collectors.toList());
            }
            return cateLog2Vos;
        }));

        //并将数据再放入缓存中
        String s = JSON.toJSONString(map);
        redisTemplateMy.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return map;
    }

    /**
     * 从数据库中查询并封装的数据
     * 加锁处理 本地锁
     * @return map
     */
    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //springBoot容器中的所有组件都是单例的
        //todo 本地锁 synchronized , JUC 等都是本地锁；在分布式情况下，必须使用分布式锁

        synchronized (this) { // todo 单体服务这样加锁是没有问题的，但是分布式的需要去优化
            //加锁以后，我们应该再去缓存中确定一次，如果没有值的话再继续查询
            /**
             * todo 加锁 保证去数据库 以及再存缓存时在同一把锁内
             */
            return getDataFromDb();
        }

    }


    private List<CategoryEntity> getParentCategories(List<CategoryEntity> categoryEntities,Long parentId){
        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> item.getParentCid().equals(parentId)).collect(Collectors.toList());
        return collect;

    }






    public Map<String, List<CateLog2Vo>> getCatalogJsonOld() {
        //一级分类
        List<CategoryEntity> levelOneCategroys = this.getLevelOneCategroys();
        Map<String, List<CateLog2Vo>> map = levelOneCategroys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> { // Map 的 Val
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> list = this.list(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentCid, v.getCatId()));
            List<CateLog2Vo> cateLog2Vos = null;
            if (CollectionUtil.isNotEmpty(list)) {
                cateLog2Vos = list.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找到当前二级分类的三级分类封装成vo
                    List<CategoryEntity> categoryEntities = baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentCid, l2.getCatId()));
                    if(CollectionUtil.isNotEmpty(categoryEntities)){
                        List<CateLog2Vo.Catelog3Vo> catelog3Vos = categoryEntities.stream().map(l3 -> {
                            //2、封装成指定格式
                            CateLog2Vo.Catelog3Vo catelog3Vo = new CateLog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        cateLog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return cateLog2Vo;
                }).collect(Collectors.toList());
            }
            return cateLog2Vos;
        }));

        return map;
    }



    //eg: 225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点ID
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if(categoryEntity.getParentCid() != 0){
            this.findParentPath(categoryEntity.getParentCid(),paths);
        }
        return paths;
    }


    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        //递归查找所有菜单的子菜单
        List<CategoryEntity> children = all.stream()
                .filter(e -> e.getParentCid().equals(root.getCatId()))
                .map(e -> {
                    //找到子菜单
                    e.setChildren(this.getChildrens(e, all));
                    return e;
                })
                .sorted((menu1,menu2)->{
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) -(menu2.getSort()==null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return children;
    }



}