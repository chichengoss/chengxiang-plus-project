package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mr.M
 * @version 1.0
 * @description 内容管理服务启动类
 * @date 2023/2/11 15:49
 */

@SpringBootApplication//只要启动测试，这个注解就会把启动类所在的包和子包所有Bean扫描到容器
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
    //http://localhost:63040/content/course/list?pageNo=1&pageSize=30
}
