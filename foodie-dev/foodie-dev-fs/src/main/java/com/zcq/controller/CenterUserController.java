package com.zcq.controller;


import com.zcq.pojo.Users;
import com.zcq.pojo.vo.UsersVO;
import com.zcq.resource.FileResource;
import com.zcq.service.FdfsService;
import com.zcq.service.center.CenterUserService;
import com.zcq.utils.CookieUtils;
import com.zcq.utils.JsonUtils;
import com.zcq.utils.ZCQJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("fdfs")
public class CenterUserController extends BaseController{
    @Autowired
    private FileResource fileResource;

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FdfsService fdfsService;

    @PostMapping("uploadFace")
    public ZCQJSONResult uploadFace(
            String userId,
            MultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String path = "";
        // 开始文件上传

        if (file != null) {
            // 获得文件上传的文件名称
            String fileName = file.getOriginalFilename();
            if (StringUtils.isNotBlank(fileName)) {

                // 文件重命名  imooc-face.png -> ["imooc-face", "png"]
                String fileNameArr[] = fileName.split("\\.");

                // 获取文件的后缀名
                String suffix = fileNameArr[fileNameArr.length - 1];

                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg") ) {
                    return ZCQJSONResult.errorMsg("图片格式不正确！");
                }
                path = fdfsService.uploadOSS(file, userId, suffix);

//                path = fdfsService.upload(file, suffix);

                System.out.println(path);
            }
        } else {
            return ZCQJSONResult.errorMsg("文件不能为空！");
        }

        if (StringUtils.isNotBlank(path)) {

            //http://192.168.196.132:8888//group1/M00/00/00/wKjEhF5j5WuASOJVAAB78X5-Hcw57.jpeg
//            String finalUserFaceUrl = fileResource.getHost() + path;
            //真实完整oss文件地址
            String finalUserFaceUrl = fileResource.getOssHost() + path;

            Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);

            UsersVO usersVO = conventUsersVO(userResult);

            CookieUtils.setCookie(request, response, "user",
                    JsonUtils.objectToJson(usersVO), true);
        } else {
            return ZCQJSONResult.errorMsg("上传头像失败");
        }

        return ZCQJSONResult.ok();
    }

}
