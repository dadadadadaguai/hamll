package com.heima.gateway.filter;

import com.heima.gateway.config.AuthProperties;
import com.heima.gateway.util.JwtTool;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;

    //过滤器
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange); //放行
        }
        //获取token
        String token = null;
        List<String> headers = request.getHeaders().get("Authorization");
        if (!CollUtils.isEmpty(headers)) {
            token = headers.get(0);
        }
        //解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        //TODO 如果有效，传递用户信息
        System.out.println("userId = " + userId);
        //放行
        return chain.filter(exchange);
    }

    private boolean isExclude(String antPath) {
        for (String pattenPath : authProperties.getExcludePaths()) {
            if (!pattenPath.equals(antPath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
