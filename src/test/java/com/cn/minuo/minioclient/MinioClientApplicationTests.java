package com.cn.minuo.minioclient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.cn.minuo.minioclient.utils.minio_utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MinioClientApplicationTests {


	@Test
	void contextLoads() {
		System.out.println(minio_utils.get_time());
	}

	@Test
	void test(){
		String a="a/b/1.jpg";
		String b="/a/2.jpg";
		String c="a/b/c/3.jpg";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("a",a);
		map.put("b",b);
		map.put("c",c);

	}

	@Test
	void time(){
		System.out.println(TimeUnit.DAYS.toSeconds(0));
	}

}
