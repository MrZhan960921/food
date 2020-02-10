package com.zcq.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("demo")
public class ApplicationController {

    final static Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    @GetMapping("/hello")
    public String hello() {

        LOGGER.info("info");
        LOGGER.debug("debug");
        LOGGER.error("error");
        LOGGER.warn("warn");
        return "Hello World";
    }

    /**
     * springsession框架会自动帮我们将session存在redis中
     * 即可以从redis中获取session信息，实现分布式session
     * @param request
     * @return
     */
    @GetMapping("/setSession")
    public Object setSession(HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("userInfo", "new User");
        session.setMaxInactiveInterval(3600);
        session.getAttribute("userInfo");
        return "ok!!";

    }

    @GetMapping("/getSession")
    public String getSession(HttpServletRequest request) {

        HttpSession session = request.getSession();
        String userInfo = (String) session.getAttribute("userInfo");
        return userInfo;

    }
}
