package com.zcq.service.impl;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.zcq.resource.FileResource;
import com.zcq.service.FdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FdfsServiceImpl implements FdfsService {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private FileResource fileResource;


    @Override
    public String upload(MultipartFile file, String fileExtName) throws Exception {

        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),
                file.getSize(),
                fileExtName,
                null);
        String path = storePath.getFullPath();

        return path;
    }

    @Override
    public String uploadOSS(MultipartFile file, String userId, String fileExtName) throws Exception {
        // 构建ossClient 流式简单上传
        OSS ossClient = new OSSClientBuilder()
                .build(fileResource.getEndpoint(),
                        fileResource.getAccessKeyId(),
                        fileResource.getAccessKeySecret());
        InputStream inputStream = file.getInputStream();

        //构建文件上传路径
        String myObjectName = fileResource.getObjectName() + "/" + userId + "/" + userId + "." + fileExtName;
        ossClient.putObject(fileResource.getBucketName(), myObjectName, inputStream);
        ossClient.shutdown();

        //fileResource.getHost()拼接后真实文件路径
        return  myObjectName;
    }
}
