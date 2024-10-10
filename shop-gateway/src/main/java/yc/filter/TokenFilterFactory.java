package yc.filter;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yc.utils.JwtTokenUtil;

import java.util.List;

@Component
public class TokenFilterFactory extends AbstractGatewayFilterFactory<TokenFilterFactory.Config> {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                if (!exchange.getRequest().getHeaders().containsKey("token")){
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                String token = exchange.getRequest().getHeaders().get("token").get(0);
                if (token == null || token.isEmpty() || jwtTokenUtil.isExpired(token)){
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
        },1);
    }

    @Data
    public static class Config{
        private String a;
        private String b;
        private String c;
    }

    public TokenFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder(){
        return List.of("a","b","c");
    }
}
