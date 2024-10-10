package yc.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

@Component
public class ServiceTimeBetweenRoutePredicateFactory extends AbstractRoutePredicateFactory<ServiceTimeBetweenRoutePredicateFactory.Config> {
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        LocalTime start = config.getStartTime();
        LocalTime end = config.getEndTime();
        return new Predicate<ServerWebExchange>() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                LocalTime now = LocalTime.now();

                return now.isAfter(start) && now.isBefore(end);
            }
        };
    }

    @Data
    public static class Config{
        private LocalTime startTime;
        private LocalTime endTime;
    }

    public ServiceTimeBetweenRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder(){
        return List.of("startTime","endTime");
    }
}
