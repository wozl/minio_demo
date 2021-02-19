package com.cn.minuo.minioclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class MinioClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinioClientApplication.class, args);
	}

	@GetMapping("/")
	public String index(){
		return "index";
	}
}
