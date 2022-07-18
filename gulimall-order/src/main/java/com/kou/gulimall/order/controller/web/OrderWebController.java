package com.kou.gulimall.order.controller.web;


import com.kou.gulimall.common.exception.NoStockException;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.order.service.OrderService;
import com.kou.gulimall.order.vo.OrderConfirmVo;
import com.kou.gulimall.order.vo.OrderSubmitVo;
import com.kou.gulimall.order.vo.SubmitOrderRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@Api(tags = "订单WEB相关")
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * todo 核心點：
     *       fegin 在調用遠程服務之前是 會有各種攔截器的
     * 構造請求，默認不會帶請求頭的
     *       
     *      當我們的fegin 去調用其他服務時是不會請求頭的； cookie 不會被携帶
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ApiOperation("去结算")
    @GetMapping("/toTrade")
    public R toTrade() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        return R.ok().put("data",confirmVo);
    }


    @ApiOperation("提交订单")
    @PostMapping("/submitOrder")
    public R submitOrder(@RequestBody OrderSubmitVo vo) {
        SubmitOrderRespVo orderRespVo = null;
        try {
            orderRespVo = orderService.submitOrder(vo);
        } catch (Exception e) {
            if(e instanceof NoStockException){
                return R.error(e.getMessage());//一定是库存不足
            }else {
                return R.error(e.getMessage());
            }
        }

        if (orderRespVo.getStatusCode() == 0) {
            return R.ok().setData(orderRespVo);
        } else if (orderRespVo.getStatusCode() == 1) {
            return R.error("操作不当");
        } else {
            return R.error("商品价格变动，请及时检查更新");

        }
    }

}
