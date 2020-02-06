package com.zcq.service;

import com.zcq.pojo.OrderStatus;
import com.zcq.pojo.bo.ShopcartBO;
import com.zcq.pojo.bo.SubmitOrderBO;
import com.zcq.pojo.vo.OrderVO;

import java.util.List;


public interface OrderService {

    /**
     * 用于创建订单相关信息
     * @param submitOrderBO
     */
    public OrderVO createOrder(List<ShopcartBO> shopcartList, SubmitOrderBO submitOrderBO);

    /**
     * 修改订单状态
     * @param orderId
     * @param orderStatus
     */
    public void updateOrderStatus(String orderId, Integer orderStatus);

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    public OrderStatus queryOrderStatusInfo(String orderId);

    /**
     * 关闭超时未支付订单
     */
    public void closeOrder();
}
