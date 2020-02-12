package com.zcq.controller;

import com.zcq.pojo.Users;
import com.zcq.pojo.vo.UsersVO;
import com.zcq.service.UserService;
import com.zcq.utils.JsonUtils;
import com.zcq.utils.MD5Utils;
import com.zcq.utils.RedisOperator;
import com.zcq.utils.ZCQJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author: chaoqun
 * @Date: 2020/2/11 23:48
 */
@Controller
public class SSOController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";


    @GetMapping("/login")
    public String login(String returnUrl,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response){
        model.addAttribute("returnUrl",returnUrl);

        //todo 后续完善是否登陆

        //用户从未登陆过 第一次则跳转到cas统一登录页面
        return "login";
    }


    /**
     * CAS的统一登陆接口
     *
     *      目的：
     *          1。登陆后创建用户的全局会话    ------  uniqueToken
     *          2。创建用户全局门票 ----用以表示在CAS端是否登陆  - -- -userTicket
     *          3.创建用户的临时票据 --用于回跳，回传     --- tmpTicket
     *
     * @param username
     * @param password
     * @param returnUrl
     * @param model
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception {

        model.addAttribute("returnUrl",returnUrl);

        //0.判断用户名密码必须不为空
        if(StringUtils.isBlank(username)
                || StringUtils.isBlank(password)){

            model.addAttribute("errmsg","用户名或者密码不能为空");
            return "login";
        }
        //1.实现注册
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));

        if (null == userResult){
            model.addAttribute("errmsg","用户名或者密码不正确");
            return "login";
        }

        //2.生成用户token，存入redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(),
                JsonUtils.objectToJson(usersVO));

        //3.生成ticket门票， 全局门票。代表用户在CAS登陆过
        String userTicket = UUID.randomUUID().toString().trim();

        //用户全局门票需要放入CAS端的cookie中,这样当下次再次登录时，可以通过cookie检测是否已经登陆过cas
        setCookie(COOKIE_USER_TICKET,userTicket,response);

        //4.userTicket关联用户ID ，并且放入到redis中，代表这个用户有门票了
        redisOperator.set(REDIS_USER_TICKET + ":" + userTicket, userResult.getId());

        //5.生成临时票据，回跳到调用端网址，是由CAS端所签发的的一个一次性的临时票据
        String tmpTicket = createTmpTicket();

        /**
         *  userTicket : 用于表示用户在CAS端的一个登陆状态，已经登陆
         *  tmpTicket: 用于颁发给用户进行一次性验证的票据，有时效性
         *
         */

        /**
         * 举例：
         *      我们去动物园玩耍，大门口买了一张统一的门票，这个就是CAS系统的全局门票和用户全局会话。
         *      动物园里有一些小的景点，需要凭你的门票去领取一次性的票据，有了这张票据以后就能去一些小的景点游玩了。
         *      这样的一个个的小景点其实就是我们这里所对应的一个个的站点。
         *      当我们使用完毕这张临时票据以后，就需要销毁。
         */
        //回跳
        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
    }


    @PostMapping("/verifyTmpTicket")
    @ResponseBody
    public ZCQJSONResult verifyTmpTicket(String tmpTicket,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {
        // 使用一次性临时票据来验证用户是否登录，如果登录过，把用户会话信息返回给站点
        // 使用完毕后，需要销毁临时票据
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)){
            return ZCQJSONResult.errorUserTicket("用户票据异常");
        }

        // 0. 如果临时票据OK，则需要销毁，并且拿到CAS端cookie中的全局userTicket，以此再获取用户会话
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))){
            return ZCQJSONResult.errorUserTicket("用户票据异常");
        }else {
            // 销毁临时票据
            redisOperator.del(REDIS_TMP_TICKET + ":" + tmpTicket);
        }
        // 1. 验证并且获取用户的userTicket
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)){
            return ZCQJSONResult.errorUserTicket("用户票据异常");
        }
        return ZCQJSONResult.ok();
    }


    /**
     * 生成临时票据
     * @return
     */
    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();
        //有过期时间
        try {
            redisOperator.set(REDIS_TMP_TICKET + ":"+ tmpTicket,
                    MD5Utils.getMD5Str(tmpTicket),600);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmpTicket;
    }

    private void setCookie(String key,
                           String val,
                           HttpServletResponse response){

        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request,
                             String key){
        Cookie[] cookies = request.getCookies();
        if (null == cookies || StringUtils.isBlank(key)){
            return null;
        }
        String cookieValue = null;

        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(key)){
                cookieValue = cookies[i].getValue();
                break;
            }
        }

        return cookieValue;

    }
}
