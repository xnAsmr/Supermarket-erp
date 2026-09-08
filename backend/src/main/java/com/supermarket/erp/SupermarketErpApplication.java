package com.supermarket.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.supermarket.erp.module.*.mapper")
@EnableScheduling
public class SupermarketErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketErpApplication.class, args);
    }
}
