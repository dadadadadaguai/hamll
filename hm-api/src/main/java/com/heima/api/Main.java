package com.heima.api;

import com.heima.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}