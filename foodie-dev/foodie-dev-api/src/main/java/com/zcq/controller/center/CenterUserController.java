package com.zcq.controller.center;


import com.zcq.controller.BaseController;
import com.zcq.pojo.Users;
import com.zcq.pojo.bo.center.CenterUserBO;
import com.zcq.service.center.CenterUserService;
import com.zcq.utils.CookieUtils;
import com.zcq.utils.JsonUtils;
import com.zcq.utils.ZCQJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户信息接口", tags = {"用户信息相关接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息", httpMethod = "POST")
    @PostMapping("update")
    public ZCQJSONResult update(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @RequestBody CenterUserBO centerUserBO,
            BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        System.out.println(centerUserBO);

        Users userResult = centerUserService.updateUserInfo(userId, centerUserBO);

        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 后续要改，增加令牌token，会整合进redis，分布式会话

        return ZCQJSONResult.ok();
    }

    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
}
