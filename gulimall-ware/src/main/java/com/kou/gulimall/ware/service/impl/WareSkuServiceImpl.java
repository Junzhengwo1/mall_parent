package com.kou.gulimall.ware.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.enume.OrderStatusEnum;
import com.kou.gulimall.common.exception.NoStockException;
import com.kou.gulimall.common.to.SkuHasStockVo;
import com.kou.gulimall.common.to.mq.OrderTo;
import com.kou.gulimall.common.to.mq.StockDetailTo;
import com.kou.gulimall.common.to.mq.StockLockedTo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.ware.dao.WareSkuDao;
import com.kou.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.kou.gulimall.ware.entity.WareOrderTaskEntity;
import com.kou.gulimall.ware.entity.WareSkuEntity;
import com.kou.gulimall.ware.feign.OrderFeignService;
import com.kou.gulimall.ware.feign.ProductFeignService;
import com.kou.gulimall.ware.service.WareOrderTaskDetailService;
import com.kou.gulimall.ware.service.WareOrderTaskService;
import com.kou.gulimall.ware.service.WareSkuService;
import com.kou.gulimall.ware.vo.OrderItemVo;
import com.kou.gulimall.ware.vo.OrderVo;
import com.kou.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> queryWrapper = Wrappers.lambdaQuery();
        String skuId = (String) params.get("skuId");
        if (StrUtil.isNotBlank(skuId)){
            queryWrapper.eq(WareSkuEntity::getSkuId,skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StrUtil.isNotBlank(wareId)) {
            queryWrapper.eq(WareSkuEntity::getWareId,wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }


    @SuppressWarnings("all")
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录就新增
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(WareSkuEntity::getSkuId,skuId);
        queryWrapper.eq(WareSkuEntity::getWareId,wareId);
        List<WareSkuEntity> list = this.list(queryWrapper);
        if(CollectionUtil.isEmpty(list) && list.size() == 0 ){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku 如果远程的调用失败的话，事务不用回滚
            // 1、我们只需自己catch掉异常
            // TODO 2、还可以用什么办法让异常出现不回滚？ 高级部分学习
            R r = productFeignService.info(skuId);
            try {
                if(r.getCode() == 0){
                    Map<String,Object> data = (Map<String, Object>) r.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程去调用异常", e);
            }
            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    @SuppressWarnings("all")
    public List<SkuHasStockVo> queryHasStock(List<Long> skuIdList) {

        List<SkuHasStockVo> vos = skuIdList.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            Long count = this.getBaseMapper().getStockNum(skuId);
            Long count2 = Optional.ofNullable(count).orElse(BigDecimal.ZERO.longValue());
            vo.setHasStock(count2 > 0);
            return vo;
        }).collect(Collectors.toList());

        return vos;
    }







    /**
     * todo 库存分布式事务的应用场景
     * 库存解锁的场景
     * 1、下单成功，订单过期没有支付被系统自动取消，被用户手动取消 此时都要解锁库存
     * 2、下单成功，库存也成功但是接下来的业务失败，导致订单回滚，那么库存也该解锁，之前用的seata实现就太慢了
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单
         * 目的为了追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //1、按照下单的收货地址，找到一个就近仓库，锁定库存

        //找到每个商品在哪个仓库都有
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(o -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = o.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(o.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 锁库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStockLocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if(CollectionUtil.isEmpty(wareIds)){
                throw new NoStockException(skuId);
            }

            //todo 1.如果每一个商品都锁定成功了，那么我们就把工作单信息记录发给ＭＱ了
            //如果锁定失败了。前面报存的工作单信息就回滚了、发出去的消息，即使要解锁记录。
            // 由于去数据查没有的话　也是不会有影响的
            //但是这是不合理的
            //解决：
                //
            for (Long wareId : wareIds) {
                //成功返回1；否则0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());
                if(count==1){
                    //锁定成功
                    skuStockLocked = true;
                    //保存库存单详情信息
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null,skuId,"",hasStock.getNum(),wareOrderTaskEntity.getId(),wareId,1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //todo 【核心】 就是该告诉MQ 库存锁定成功了
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetailTo);
                    stockLockedTo.setStockDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                    break;
                } //当前仓库锁失败；尝试下一个仓

            }

            if(!skuStockLocked){
                //当前商品所有仓库都没锁住
                throw new NoStockException(skuId);
            }

        }
        return true;
    }

    @Override
    public void releaseLockStock(StockLockedTo to) {
        //库存工作单的id
//        Long id = to.getId();
        StockDetailTo stockDetailTo = to.getStockDetailTo();
        Long stockDetailId = stockDetailTo.getId();
        //查询数据库关于这个订单的锁定库存信息
        //有：
        //证明库存锁定成功了
        //解锁：
        //1、没有这个订单的话，必须解锁
        //2、有这个订单
        //看订单是否取消；决定是否解锁
        //没有： 那说明是锁定失败了，库存回滚了。这种情况无需解锁
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(stockDetailId);
        if (ObjectUtil.isNotNull(taskDetailEntity)) {
            //解锁
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(to.getId());
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData("data", new TypeReference<OrderVo>() {});
                if (ObjectUtil.isNull(data) || data.getStatus().equals(OrderStatusEnum.CANCLED.getCode())) {
                    //订单是取消状态的话，才解锁操作
                    if(taskDetailEntity.getLockStatus()==1){
                        this.unLockStock(taskDetailEntity);
                    }
                }
            }else {
                throw new RuntimeException("远程服务失败……");
            }
        }


    }


    /**
     * 防止服务器卡顿，导致订单消息一直改不了，而库存服务优先到期
     * @param to
     */
    @Override
    public void releaseLockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        //查一下库存最新状态，防止重复解锁库存
        WareOrderTaskEntity entity = wareOrderTaskService.getOne(new LambdaQueryWrapper<WareOrderTaskEntity>().eq(WareOrderTaskEntity::getOrderSn, orderSn));
        List<WareOrderTaskDetailEntity> taskDetailEntities = wareOrderTaskDetailService.list(new LambdaQueryWrapper<WareOrderTaskDetailEntity>()
                .eq(WareOrderTaskDetailEntity::getTaskId, entity.getId())
                .eq(WareOrderTaskDetailEntity::getLockStatus, 1));
        taskDetailEntities.forEach(this::unLockStock);

    }


    @Transactional
    public void unLockStock(WareOrderTaskDetailEntity taskDetailEntity){
        //库存解锁
        wareSkuDao.unLockStock(taskDetailEntity);
        //更新库存工作单的状态
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }


    /**
     * 内部类
     *
     */
    @Data
    static
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}