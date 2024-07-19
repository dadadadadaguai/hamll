package com.heima.cart.common;

import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

//采用随机轮询进行服务发现
@RequiredArgsConstructor
public class ServiceSearch {

    private static final DiscoveryClient discoveryClient = null;

    public  static ServiceInstance getDiscoveryClient(DiscoveryClient discoveryClient,String serviceInstance) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceInstance);
        return instances.get(RandomUtil.randomInt(instances.size()));
    }
}
