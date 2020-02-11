package com.zcq.interceptor;

import com.zcq.utils.JsonUtils;
import com.zcq.utils.RedisOperator;
import com.zcq.utils.ZCQJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 拦截器校验token
 * @Author: chaoqun
 * @Date: 2020/2/11 21:05
 */
public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    /**
     * 拦截请求在访问controller之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //token相关信息放入请求header中,前端header中相关token信息从cookie中取
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");


        if (StringUtils.isNotBlank(userId) &&
                StringUtils.isNotBlank(userToken)
        ){
            String uniqueToken = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(uniqueToken)){
                returnErrorResponse(response,ZCQJSONResult.errorMsg("请登录..."));
                return false;
            }else {
                if (!uniqueToken.equals(userToken)){
                    returnErrorResponse(response,ZCQJSONResult.errorMsg("账号可能在异地登陆"));
                    return false;
                }
            }
        }else {
            returnErrorResponse(response,ZCQJSONResult.errorMsg("请登录..."));
            return false;
        }
        /**
         * false ：请求被拦截
         * true： 请求经过校验以后，放行
         */
        return true;
    }

    public void returnErrorResponse(HttpServletResponse response, ZCQJSONResult result){

        OutputStream out = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != out){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
