package com.kou.gulimall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kou.gulimall.common.constant.OrderServerConstant;
import com.kou.gulimall.common.exception.NoStockException;
import com.kou.gulimall.common.to.SkuHasStockVo;
import com.kou.gulimall.common.to.mq.OrderTo;
import com.kou.gulimall.common.utils.PageUtils;
import com.kou.gulimall.common.utils.Query;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.common.vo.MemberRespVo;
import com.kou.gulimall.order.dao.OrderDao;
import com.kou.gulimall.order.entity.OrderEntity;
import com.kou.gulimall.order.entity.OrderItemEntity;
import com.kou.gulimall.order.enume.OrderStatusEnum;
import com.kou.gulimall.order.feign.CartFeignService;
import com.kou.gulimall.order.feign.MemberFeignService;
import com.kou.gulimall.order.feign.ProductFeignService;
import com.kou.gulimall.order.feign.WareFeginService;
import com.kou.gulimall.order.interceptor.LoginUserInterCeptor;
import com.kou.gulimall.order.service.OrderItemService;
import com.kou.gulimall.order.service.OrderService;
import com.kou.gulimall.order.to.OrderGreateTo;
import com.kou.gulimall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> voInfo = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WareFeginService wareFeginService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * todo 这里 由于是异步的就不是同一个线程
     *      那么我们的拦截器 就相当于失效
     * 【也就是说，异步的话，fegin 会丢失上下文**************超级关键】
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //todo 获取到当前用户信息
        MemberRespVo memberRespVo = LoginUserInterCeptor.loginUser.get();
        //主线程 拿到请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //远程获取会员的收获地址
            //子线程共享数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setMemberAddressVos(address);
        }, executor);

        CompletableFuture<Void> getCurrentCartItems = CompletableFuture.runAsync(() -> {
            //远程获取购物车数据
        /**
         * todo 這個地方有個bug 就是當我們去購物車 查時，購物車服務目前感知不到是否是登錄狀態。
         * todo feign 遠程調用時默認不會帶了 請求頭
         */
            //子线程共享数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setOrderItemVos(cartItems);
            confirmVo.setTotal(confirmVo.calTotal());
            confirmVo.setPayPrice(confirmVo.calPayPrice());
            confirmVo.setNum(confirmVo.calNum());
        }, executor).thenRunAsync(()->{
            //查询库存信息
            List<OrderItemVo> orderItemVos = confirmVo.getOrderItemVos();
            List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeginService.queryHasStock(skuIds);
            List<SkuHasStockVo> data = r.getData("data", new TypeReference<List<SkuHasStockVo>>() {});
            Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            confirmVo.setStockMap(map);
        });

        //todo 用户积分 从当前用户信息中获取
        confirmVo.setIntegeration(memberRespVo.getIntegration());

        //todo 防重令牌 使用redis来实现
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderServerConstant.ORDER_TOKEN+memberRespVo.getId(),token,20, TimeUnit.MINUTES);

        CompletableFuture.allOf(getAddressFuture,getCurrentCartItems).get();

        return confirmVo;
    }






    /**
     * todo 下单操作 注意点
     * todo 【事务的全面的应用场景】 分布式事务 主要出现的问题就是 网络问题造成的
     *   1、远程服务假失败
     *   2、远程执行成功，但是下面的其他方法出现问题
     *
     *
     *
     * @param vo
     * @return
     */
    // Transactional 是本地事务
    //@GlobalTransactional
    /**
     * todo 由于下订单属于大并发操作;所以这里的seata AT 模式是不适用的
     *      ：解决：那么我们的下订单这种场景的话采取 【柔性事务-可靠消息-最终一致性方案】********【核心】
     *
     */
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo vo) {
        voInfo.set(vo);
        log.info("订单源数据-》{}", JSON.toJSONString(vo));
        SubmitOrderRespVo orderRespVo = new SubmitOrderRespVo();
        orderRespVo.setStatusCode(0);
        MemberRespVo memberRespVo = LoginUserInterCeptor.loginUser.get();
        String orderTokenCome = vo.getOrderToken();
        //保证原子性的脚本
        String script= "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String key = OrderServerConstant.ORDER_TOKEN + memberRespVo.getId();
        //1、验证vo【要保证原子性】
        //0,1 0表示失败
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(key), orderTokenCome);
        if(result == 0L){
            //验证失败
            orderRespVo.setStatusCode(1);
        }else {
            //验证成功 正式操作
            //1、创建订单 订单项等
            OrderGreateTo to = this.greateOrder();
            //2、验价
            BigDecimal payAmount = to.getOrderEntity().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            double v = payAmount.subtract(payPrice).abs().doubleValue();
            if(v < 0.01){
                //金额对比成功
                //todo  入库【分布式事务】
                this.saveOrder(to);
                //todo 库存锁定 只要有异常回滚订单数据
                //远程调用 锁库存操作
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(to.getOrderEntity().getOrderSn());
                List<OrderItemVo> collect = to.getItemEntities().stream().map(o -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(o.getSkuId());
                    itemVo.setCount(o.getSkuQuantity());
                    itemVo.setTitle(o.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(collect);
                //todo 远程锁库存【分布式事务】 为了满足高并发
                //todo 让库存服务自己去回滚；如果失败了就去发消息让库存知道 自己去解锁库存
                // todo 让库存服务自己去解锁；使用消息队列来实现的*********【核心】
                R r = wareFeginService.lockOrder(lockVo);
                if(r.getCode()==0){
                    //锁库存成功【对应的订单信息】
                    //int i = 10/0;
                    orderRespVo.setOrderEntity(to.getOrderEntity());
                    //todo 订单创建成功 发消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",to.getOrderEntity());
                    log.info("订单{},提交成功数据---》{}",orderRespVo.getOrderEntity().getOrderSn(),JSON.toJSONString(orderRespVo));
                }else {
                    //锁库失败
                    //抛异常保证数据回滚
                    log.error("{}提交订单失败",r.get("msg"));
                    throw new NoStockException((String) r.get("msg"));

                }
            }else {
                orderRespVo.setStatusCode(2);
            }
        }

        return orderRespVo;
        //下面是常规校验 不能保证原子性
        /*
        if(StrUtil.isNotEmpty(orderTokenCome) && Objects.equals(orderToken, orderTokenCome)){
            //验证令牌通过

            redisTemplate.delete(key);
        }else {
            log.error("下单失败");
        }
         */

    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //先判断当前订单的最新状态
        OrderEntity entity = this.getById(orderEntity.getId());
        if(entity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtil.copyProperties(entity,orderTo);
            try {
                //todo 保证消息一定会发送出去，每一个发送的消息我们都做好日志记录（入库保存每一个消息的详细信息，我们可以定期扫描数据库吗，将失败的消息重新发送）
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
            } catch (AmqpException e) {
                //出现问题 todo  我们重试发送

            }
        }

    }
    /**
     * 正式入库
     * @param to
     */

    private void saveOrder(OrderGreateTo to) {
        OrderEntity orderEntity = to.getOrderEntity();
        List<OrderItemEntity> itemEntities = to.getItemEntities();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        orderItemService.saveBatch(itemEntities);

    }


    /**
     * 创建订单
     * @return
     */
    private OrderGreateTo greateOrder(){
        OrderGreateTo to = new OrderGreateTo();
        //生产订单号
        String orderNum = IdWorker.getTimeId();
        //构建带订单
        OrderEntity orderEntity = this.buildOrder(orderNum);
        to.setOrderEntity(orderEntity);
        //构建订单项
        List<OrderItemEntity> orderItemEntities = this.buildOrderItems(orderNum);
        to.setItemEntities(orderItemEntities);
        //计算价格积分等相关信息
        this.computePrice(orderEntity, Objects.requireNonNull(orderItemEntities));

        return to;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //订单的总额，每一项的总额
        double sum = orderItemEntities.stream().mapToDouble(orderItemEntity -> orderItemEntity.getRealAmount().doubleValue()).sum();
        BigDecimal coupon = new BigDecimal("0.00");
        BigDecimal integration = new BigDecimal("0.00");
        BigDecimal promotion = new BigDecimal("0.00");
        Integer gift = 0;
        Integer growth = 0;
        for (OrderItemEntity itemEntity : orderItemEntities) {
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            gift += itemEntity.getGiftIntegration();
            growth += itemEntity.getGiftGrowth();
        }

        BigDecimal totalAmount = new BigDecimal(sum);
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setPayAmount(totalAmount.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);

        //设置积分
        orderEntity.setIntegration(gift);
        orderEntity.setGrowth(growth);

    }

    private OrderEntity buildOrder(String orderNum) {
        OrderEntity orderEntity = new OrderEntity();
        MemberRespVo memberRespVo = LoginUserInterCeptor.loginUser.get();
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setOrderSn(orderNum);
        //远程运费信息
        R fare = wareFeginService.getFare(voInfo.get().getAddrId());
        FareVo data = fare.getData("data", new TypeReference<FareVo>() {});
        //运费信息
        orderEntity.setFreightAmount(data.getFare());
        //收获人信息
        orderEntity.setReceiverCity(data.getMemberAddressVo().getCity());
        orderEntity.setReceiverDetailAddress(data.getMemberAddressVo().getDetailAddress());
        orderEntity.setReceiverName(data.getMemberAddressVo().getName());
        orderEntity.setReceiverPhone(data.getMemberAddressVo().getPhone());
        orderEntity.setReceiverPostCode(data.getMemberAddressVo().getPostCode());
        orderEntity.setReceiverProvince(data.getMemberAddressVo().getProvince());
        orderEntity.setReceiverRegion(data.getMemberAddressVo().getRegion());
        //订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        orderEntity.setDeleteStatus(0);

        return orderEntity;
    }


    /**
     * 构建订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderNum) {
        //获取所有订单项
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if(CollectionUtil.isNotEmpty(cartItems)){
            return cartItems.stream().map(o->{
                OrderItemEntity orderItemEntity = this.buildOrderItem(o);
                orderItemEntity.setOrderSn(orderNum);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }else {
            return null;
        }
    }

    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //商品的spu信息
        Long skuId = orderItemVo.getSkuId();
        //远程查询
        R r = productFeignService.SpuInfoForSkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData("data", new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        //商品的sku信息
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImge());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttr(),";"));
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        // 优惠信息 忽略

        //积分
        orderItemEntity.setGiftGrowth(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());
        orderItemEntity.setGiftIntegration(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());

        //价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.00"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.00"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.00"));
        //当前订单项的实际金额
        BigDecimal totalPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = totalPrice.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }














    /**
     * TODO  本地事务失效演示代码
     * 核心代码
     */
    @Transactional
    public void  a(){
        //用代理对象来调用
        OrderServiceImpl o = (OrderServiceImpl) AopContext.currentProxy();
        o.b();
        o.c();
    }
    @Transactional
    public void  b(){

    }
    @Transactional
    public void  c(){

    }
}