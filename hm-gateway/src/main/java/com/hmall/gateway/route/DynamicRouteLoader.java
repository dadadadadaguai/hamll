package com.hmall.gateway.route;


import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private NacosConfigManager nacosConfigManager;
    private RouteDefinitionWriter writer;
    //路由配置
    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    //路由信息
    private final Set<String> routeIds = new HashSet<>();

    //初始化路由监听器
    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dataId, group, 5000,
                new Listener() {
                    //获取监听器的线程池
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        updateConfigInfo(configInfo);
                    }
                });
        updateConfigInfo(configInfo);
    }

    /**
     * 更新路由表
     *
     * @param configInfo
     */
    private void updateConfigInfo(String configInfo) {
        log.debug("更新路由表配置：{}", configInfo);
        List<RouteDefinition> routeDefinitionList = JSONUtil.toList(configInfo, RouteDefinition.class);
        //更新
        //先删除原先的路由表
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        if (CollUtils.isEmpty(routeDefinitionList)) {
            // 无新路由配置，直接结束
            return;
        }
        //更新路由
        routeDefinitionList.forEach(routeDefinition -> {
            writer.save(Mono.just(routeDefinition)).subscribe();
            routeIds.add(routeDefinition.getId());
        });
    }
}
