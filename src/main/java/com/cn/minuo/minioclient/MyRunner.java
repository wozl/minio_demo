package com.cn.minuo.minioclient;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
@Slf4j
public class MyRunner implements ApplicationRunner {

    //oss服务url
    @Value("${minio.conf.url}")
    String url;

    //用户名
    @Value("${minio.conf.user}")
    String user;

    //密码
    @Value("${minio.conf.password}")
    String password;

    //存储的路径
    @Value("${minio.conf.path}")
    String path;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            // 检查oss云存储对象中是否包含指定文件夹，若不包含创建后才能进行文件操作
            boolean isExist =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(path).build());
            if(isExist) {
                System.out.println("Bucket already exists.");
                log.warn("上传目录已存在!");
            } else {
                // 不存在指定目录，就自动进行创建
                log.warn("目录不存在，开始自动创建默认目录!");
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(path).build());
            }
        }catch (Exception e){
            log.error(e.toString());
            log.error("获取云对象存储根目录文件夹对象失败！");
        }
    }
}
