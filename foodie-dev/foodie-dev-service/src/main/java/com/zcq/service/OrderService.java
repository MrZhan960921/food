package com.zcq.service;

import com.zcq.pojo.OrderStatus;
import com.zcq.pojo.bo.SubmitOrderBO;


public interface OrderService {

    /**
     * 用于创建订单相关信息
     * @param submitOrderBO
     */
    public void createOrder(SubmitOrderBO submitOrderBO);


}
