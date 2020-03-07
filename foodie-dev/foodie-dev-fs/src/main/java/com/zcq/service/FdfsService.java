package com.zcq.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: chaoqun
 * @Date: 2020/3/7 21:55
 */
public interface FdfsService {

    public String upload(MultipartFile file, String fileExtName) throws Exception;

    public String uploadOSS(MultipartFile file, String userId, String fileExtName) throws Exception;

}
