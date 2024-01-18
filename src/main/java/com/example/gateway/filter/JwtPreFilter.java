package com.example.gateway.filter;

import com.example.gateway.dto.JwtTokenResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class JwtPreFilter extends AbstractGatewayFilterFactory<JwtPreFilter.Config> {

    private final RouteValidator routeValidator;

    private final WebClient.Builder webClientBuilder;

    public JwtPreFilter(RouteValidator routeValidator, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (routeValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization header");
                }
                String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                return webClientBuilder.build().get()
                        .uri("http://authentication/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .retrieve()
                        .bodyToMono(JwtTokenResponse.class)
                        .map(response -> {
                            System.out.println("username - " + response.getLogin());
                            System.out.println("role - " + response.getAuthorities().get(0));
                            exchange.getRequest().mutate().header("username", response.getLogin());
                            exchange.getRequest().mutate().header("authorities", response.getAuthorities()
                                    .get(0).toString());
                            return exchange;

                        }).flatMap(chain::filter).onErrorResume(error -> {
                            return Mono.error(new RuntimeException());
                        });
            }
            return chain.filter(exchange);
        });

    }

    public static class Config {

    }

}
