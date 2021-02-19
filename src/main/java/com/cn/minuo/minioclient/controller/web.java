package com.cn.minuo.minioclient.controller;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.google.gson.Gson;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import javafx.scene.chart.ValueAxis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cn.minuo.minioclient.utils.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/view")
@Slf4j
public class web {

    @Autowired
    private Gson js;

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

    @GetMapping(value = "/test",produces = "application/json;charset=UTF-8")
    public String test(){
        Gson json=new Gson();
        Map<String,Object> map= new HashMap<String, Object>();
        map.put("code",200);
        map.put("msg","success");
        return json.toJson(map);
    }

    /**
     * 上传
     * @param file
     * @return
     */
    @PostMapping(value = "/up",produces = "multipart/form-data;charset=UTF-8")
    public String upload(@RequestParam(value = "file",defaultValue = "",required = false) MultipartFile file,@RequestParam(value ="bucket_path",defaultValue = "",required = false) String bucket_path){
        Gson gs= new Gson();
        Map<String,Object> mp = new HashMap<String,Object>();
        try {
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            if(!(bucket_path == "")){
                // 检查oss云存储对象中是否包含指定文件夹，若不包含创建后才能进行文件操作
                boolean isExist =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket_path).build());
                if(isExist) {
                    System.out.println("Bucket already exists.");
                    log.warn("上传目录已存在!");
                } else {
                    // 不存在指定目录，就自动进行创建
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket_path).build());
                }
                String suf=file.getOriginalFilename();
                suf=suf.substring(suf.lastIndexOf(".")); //获取文件扩展名
                String filename=minio_utils.get_time()+"-"+minio_utils.uuid()+suf;
                log.warn("文件名："+filename);
                // Upload the zip file to the bucket with putObject
                minioClient.putObject(PutObjectArgs.builder().bucket(bucket_path).object(filename).stream(
                        file.getInputStream(),file.getSize(),-1).
                        contentType(file.getContentType()).build());
                file.getInputStream().close();
                mp.put("code",200);
                mp.put("msg","上传成功！");
                mp.put("url",url+"/"+bucket_path+"/"+filename);
            }else{ //若不指定目录，则使用服务端默认指定的目录
                // 检查oss云存储对象中是否包含指定文件夹，若不包含创建后才能进行文件操作
                boolean isExist =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(path).build());
                if(isExist) {
                    System.out.println("Bucket already exists.");
                    log.warn("上传目录已存在!");
                } else {
                    // 不存在指定目录，就自动进行创建
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(path).build());
                }

                String suf=file.getOriginalFilename();
                suf=suf.substring(suf.lastIndexOf(".")); //获取文件扩展名
                String filename=minio_utils.get_time()+"-"+minio_utils.uuid()+suf;
                log.warn("文件名："+filename);
                // Upload the zip file to the bucket with putObject
                minioClient.putObject(PutObjectArgs.builder().bucket(path).object(filename).stream(
                        file.getInputStream(),file.getSize(),-1).
                        contentType(file.getContentType()).build());
                file.getInputStream().close();
                mp.put("code",200);
                mp.put("msg","上传成功！");
                mp.put("url",url+"/"+path+"/"+filename);
            }


        } catch(MinioException e) {
            System.out.println("Error occurred: " + e);
            log.error("MinioException 错误");
            log.warn(file.getContentType());
            mp.put("code",-1);
            mp.put("msg","MinioException err,上传失败，请稍后再试！");
        }catch (NoSuchAlgorithmException e){
            System.out.println("NoSuchAlgorithmException err"+e);
            log.error("NoSuchAlgorithmException错误异常");
            mp.put("code",-1);
            mp.put("msg","NoSuchAlgorithmException err,上传失败，请稍后再试！");
        }catch (IOException e){
            System.out.println("IOException"+e);
            log.error("IOException 异常");
            mp.put("code",-1);
            mp.put("msg","IOException err,上传失败，请稍后再试！");
        }catch (InvalidKeyException e){
            System.out.println("InvalidKeyException"+e);
            log.error("InvalidKeyException 错误异常");
            mp.put("code",-1);
            mp.put("msg","InvalidKeyException err,上传失败，请稍后再试！");
        }
        return gs.toJson(mp);
    }

    /**
     * 获取云对象存储桶列表
     * @return
     */
    @PostMapping(value = "/list")
    public String getBucketList(){
        Gson json = new Gson();
        Map<String,Object> map = new HashMap<String, Object>();
        try{
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            map.put("code",200);
            map.put("msg","success");
            map.put("data",minioClient.listBuckets());
        }catch (Exception e){
            log.warn("获取云对象存储根目录文件夹对象失败");
            log.error(e.toString());
            map.put("code",-1);
            map.put("msg","fail");
            map.put("data","");
        }
        return json.toJson(map);
    }

    /**
     * 删除桶，包含桶中所有的文件
     * @return
     */
    @PostMapping(value = "/del")
    public String delBucket(){
        Map<String,Object> map = new HashMap<String, Object>();
        try{
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            minioClient.deleteBucketPolicy(DeleteBucketPolicyArgs.builder().bucket("zips").build());
            //minioClient.removeBucket(RemoveBucketArgs.builder().bucket("zips").build()); //仅能删除空桶
            //minioClient.removeObject(RemoveObjectArgs.builder().bucket(path).object("张三2020-07-09-a6e185fcf1c843f0bf5d9c8523f437a1.jpg").build());
        }catch (Exception e){
            log.error("删除Bucket，请检查错误日志!");
            log.error(e.toString());
            map.put("code",-1);
            map.put("msg","fail");
        }
        return js.toJson(map);
    }

    /**
     * 获取桶中文件列表
     * @return
     */
    @PostMapping(value = "/bucketlist")
    public String ListBucketObject(@RequestParam(value = "uri",defaultValue = "images",required = false) String uri){
        Map<String,Object> mp = new HashMap<String, Object>();
        if(uri!="images"){
            path=uri;
        }
        try{
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            //获取云存储对象桶中的所有对象信息
            Iterable<Result<Item>> results=minioClient.listObjects(path);
            Map<String,Object> map = new HashMap<String, Object>();
            for(Result<Item> result : results){
                log.warn("images下的对象信息:"+ URLDecoder.decode(result.get().objectName(),"UTF-8"));
                map.put(URLDecoder.decode(result.get().objectName(),"UTF-8"),
                        minioClient.statObject(StatObjectArgs.builder().
                                bucket(path).
                                object(URLDecoder.decode(result.get().objectName(),"UTF-8")).
                        build())
                );
            }
            System.out.println(results);
            mp.put("code",200);
            mp.put("msg","success");
            mp.put("data",map);
        }catch (Exception e){
            log.error("获取云对象存储失败，请检查日志!");
            log.error(e.toString());
            mp.put("code",-1);
            mp.put("msg","faile");
            mp.put("data","");
        }
        return js.toJson(mp);
    }

    /**
     * 文件下载
     * @param filename
     * @param response
     * @throws IOException
     */
    @GetMapping(value = "/downloadfiles")
    public void download(@RequestParam(value = "filename",defaultValue = "",required = false)String filename, HttpServletResponse response) throws IOException {
        Map<String,Object> mp = new HashMap<String, Object>();
        if(!filename.isEmpty()){
            try{
                //创建oss对象，并将认证信息提交
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(url)
                        .credentials(user,password)
                        .build();
                InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(path).object(filename).build());
                response.reset();
                response.setHeader("Content-Disposition","attachment;filename="+filename);
                response.setContentType("application/force-download");
                response.setCharacterEncoding("UTF-8");
                byte buf[] = new byte[1024];
                int length = 0;
                OutputStream outputStream = response.getOutputStream();
                while ((length=stream.read(buf))>0){
                    outputStream.write(buf,0,length);
                }
                outputStream.close();
            }catch (Exception e){
                log.error(e.toString());
                log.error("文件下载失败！");
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                mp.put("code",-1);
                mp.put("msg","文件下载失败，请检查文件名是否完整！(包含文件后缀)");
                response.getWriter().write(js.toJson(mp));
            }
        }else{
            mp.put("code",-1);
            mp.put("msg","非法请求！文件名不能为空!");
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(js.toJson(mp));
        }
    }

    /**
     * 设置文件分享外链时间
     * @param filename 文件名(必须参数，需要带后缀)
     * @param day 有效天数，默认0，最大7天有效
     * @param hours 有效小时，默认0
     * @param minutes 有效分钟，默认1分钟
     * @return
     */
    @PostMapping(value = "/getshareurl")
    public String getObjectURL(@RequestParam(value = "filename") String filename,
                               @RequestParam(value = "day",defaultValue = "0",required = false)Integer day,
                               @RequestParam(value = "hours",defaultValue = "0",required = false)Integer hours,
                               @RequestParam(value = "minutes",defaultValue = "1",required =false )Integer minutes
    ){
        Map<String,Object> map = new HashMap<String, Object>();
        String shareurl="";
        Long times;
        if(day<7){
            times=TimeUnit.DAYS.toSeconds(day)+TimeUnit.HOURS.toSeconds(hours)+TimeUnit.MINUTES.toSeconds(minutes);
            try{
                //创建oss对象，并将认证信息提交
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(url)
                        .credentials(user,password)
                        .build();
                shareurl=minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder().
                                method(Method.GET).
                                bucket(path).
                                object(filename).
                                expiry(times.intValue()).
                                build());
                map.put("code",200);
                map.put("msg","success");
                map.put("url",shareurl);
            }catch (Exception e){
                log.warn("获取云对象存储根目录文件夹对象失败");
                log.error(e.toString());
                map.put("code",-1);
                map.put("msg","fail");
                map.put("url",shareurl);
            }
        }else{
            times=TimeUnit.DAYS.toSeconds(day);
            try{
                //创建oss对象，并将认证信息提交
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(url)
                        .credentials(user,password)
                        .build();
                shareurl=minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder().
                                method(Method.GET).
                                bucket(path).
                                object(filename).
                                expiry(times.intValue()).
                                build());
                map.put("code",200);
                map.put("msg","success");
                map.put("url",shareurl);
            }catch (Exception e){
                log.warn("获取云对象存储根目录文件夹对象失败");
                log.error(e.toString());
                map.put("code",-1);
                map.put("msg","fail");
                map.put("url",shareurl);
            }
        }
        return js.toJson(map);
    }

    /**
     * 获取文件详细信息
     * @param filename
     * @return
     */
    @PostMapping(value = "/fileinfo")
    public String getfileinfo(@RequestParam(value = "filename",defaultValue = "",required = false)String filename){
        Map<String,Object> mp = new HashMap<String, Object>();
        try {
            //创建oss对象，并将认证信息提交
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(user,password)
                    .build();
            ObjectStat objectStat = minioClient.statObject(StatObjectArgs.builder().
                    bucket(path).
                    object(filename).
                    build());
            mp.put("code",200);
            mp.put("msg","success");
            mp.put("data",objectStat);
        }catch (Exception e){
            log.error(e.toString());
            log.warn("获取云对象存储根目录文件夹对象失败");
            mp.put("code",-1);
            mp.put("msg","fail");
        }
        return js.toJson(mp);
    }

    /**
     * 删除文件，支持单个或者批量
     * @param data
     * @return
     */
    @PostMapping(value = "/delfiles")
    public String delfiles(@RequestBody(required = false)List<String> data){
        Map<String,Object> mp = new HashMap<String, Object>();
        if(data.size()>0){
            //批量删除文件对象
            List<DeleteObject> objects = new LinkedList<>();
            for(String tmp : data){
                if(tmp!="" && tmp!=null){
                    objects.add(new DeleteObject(tmp));
                }
            }
            if(objects.size()>0){
                try {
                    //创建oss对象，并将认证信息提交
                    MinioClient minioClient = MinioClient.builder()
                            .endpoint(url)
                            .credentials(user,password)
                            .build();
                    Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                            RemoveObjectsArgs.builder().
                                    bucket(path).
                                    objects(objects).
                                    build());
                    List<DeleteError> list = new ArrayList<>();
                    for(Result<DeleteError> result :results){
                        list.add(result.get());
                        DeleteError error = result.get();
                        System.out.println("Error in deleting object " + error.objectName() + "; " + error.message());
                    }
                    if(list.size()>0){
                        mp.put("code",-1);
                        mp.put("msg","文件删除失败，请稍后再试!");
                    }else{
                        mp.put("code",200);
                        mp.put("msg","文件删除成功!");
                    }
                }catch (Exception e){
                    log.error(e.toString());
                    log.warn("获取云对象存储根目录文件夹对象失败");
                    mp.put("code",-1);
                    mp.put("msg","fail");
                }
            }else{
                mp.put("code",-1);
                mp.put("msg","非法请求!参数不能为空!");
            }

        }else {
            mp.put("code",-1);
            mp.put("msg","非法请求!参数不能为空!");
        }
        return js.toJson(mp);
    }


    /**
     * 创建新桶
     * @param name
     * @return
     */
    @PostMapping(value = "/create_bucket")
    public String create_bucket(@RequestParam(value = "name",defaultValue = "",required = false)String name){
        Map<String,Object> mp = new HashMap<>();
        if(name==""){
            mp.put("code",-1);
            mp.put("msg","请求参数不能为空!");
        }else{
            try{
                //创建oss对象，并将认证信息提交
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(url)
                        .credentials(user,password)
                        .build();
                // 检查oss云存储对象中是否包含指定文件夹，若不包含创建后才能进行文件操作
                boolean isExist =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
                if(isExist) {
                    System.out.println("Bucket already exists.");
                    log.warn("上传目录已存在!");
                    mp.put("code",1001);
                    mp.put("msg","目录已存在，请更改新目录名称!");
                } else {
                    // 不存在指定目录，就自动进行创建
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
                    mp.put("code",200);
                    mp.put("msg","创建成功!");
                }
            }catch (Exception e){
                log.error(e.toString());
                log.error("获取云对象存储根目录文件夹对象失败！");
                mp.put("code",-1);
                mp.put("msg","服务繁忙，请稍后再试!");
            }
        }
        return js.toJson(mp);
    }
}
