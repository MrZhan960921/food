package com.zcq.controller;


import com.zcq.enums.PayMethod;
import com.zcq.pojo.OrderStatus;
import com.zcq.pojo.bo.SubmitOrderBO;
import com.zcq.service.OrderService;
import com.zcq.utils.ZCQJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    final static Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrderService orderService;


}
