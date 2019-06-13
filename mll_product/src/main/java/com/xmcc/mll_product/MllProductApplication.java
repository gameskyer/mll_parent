package com.xmcc.mll_product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@MapperScan("com.xmcc.mll_product.mapper")
public class MllProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MllProductApplication.class, args);
    }

}
