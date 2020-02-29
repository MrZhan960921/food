package com.zcq.controller;


import com.zcq.enums.OrderStatusEnum;
import com.zcq.enums.PayMethod;
import com.zcq.pojo.OrderStatus;
import com.zcq.pojo.bo.ShopcartBO;
import com.zcq.pojo.bo.SubmitOrderBO;
import com.zcq.pojo.vo.MerchantOrdersVO;
import com.zcq.pojo.vo.OrderVO;
import com.zcq.service.OrderService;
import com.zcq.utils.CookieUtils;
import com.zcq.utils.JsonUtils;
import com.zcq.utils.RedisOperator;
import com.zcq.utils.ZCQJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    final static Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisOperator redisOperator;
    /**
     * 调用支付中心的接口
     */
    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public ZCQJSONResult create(
            @RequestBody SubmitOrderBO submitOrderBO,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
                && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type) {
            return ZCQJSONResult.errorMsg("支付方式不支持！");
        }

//        System.out.println(submitOrderBO.toString());
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopcartJson)) {
            return ZCQJSONResult.errorMsg("购物数据不正确");
        }
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

        // 1. 创建订单
        OrderVO orderVO = orderService.createOrder(shopcartList, submitOrderBO);
        String orderId = orderVO.getOrderId();
        List<ShopcartBO> toBeRemovedShopcartLIst = orderVO.getToBeRemovedShopcartLIst();

        // 2. 创建订单以后，移除购物车中已结算（已提交）的商品
        /**
         * 1001
         * 2002 -> 用户购买
         * 3003 -> 用户购买
         * 4004
         */

        //清理覆盖现有的redis中的购物车数据
        shopcartList.removeAll(toBeRemovedShopcartLIst);
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
        // 整合redis之后，完善购物车中已结算商品的移除，并且同步到前端的cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, "", true);

        // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);
        // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("zcqUserId", "com.zcq");
        headers.add("password", "com.zcq");

        HttpEntity<MerchantOrdersVO> entity =
                new HttpEntity<>(merchantOrdersVO, headers);

        ResponseEntity<ZCQJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl,
                        entity,
                        ZCQJSONResult.class);
        ZCQJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200) {
            logger.error("发送错误：{}", paymentResult.getMsg());
            return ZCQJSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }

        return ZCQJSONResult.ok(orderId);
    }

    /**
     * 订单支付回调通知，修改订单状态
     *
     * @param merchantOrderId
     * @return
     */
    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    /**
     * 查询订单信息，用于支付后轮询到支付成功，跳转支付成功页面
     *
     * @param orderId
     * @return
     */
    @PostMapping("getPaidOrderInfo")
    public ZCQJSONResult getPaidOrderInfo(String orderId) {

        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return ZCQJSONResult.ok(orderStatus);
    }
}
