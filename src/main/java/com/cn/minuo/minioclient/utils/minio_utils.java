package com.cn.minuo.minioclient.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public  class minio_utils {

    public static String get_time(){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
    }

    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
