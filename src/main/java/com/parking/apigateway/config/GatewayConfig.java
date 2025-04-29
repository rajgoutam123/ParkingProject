package com.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth_service", r -> r.path("/api/auth/**")
                    .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()).setAuthenticationRequired(false)))
                    .uri("lb://AUTH-SERVICE"))
            .route("parking_service", r -> r.path("/api/parking/**")
                    .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                    .uri("lb://PARKING-SERVICE"))
            .route("booking_service", r -> r.path("/api/booking/**")
                    .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                    .uri("lb://BOOKING-SERVICE"))
            .route("payment_service", r -> r.path("/api/payment/**")
                    .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                    .uri("lb://PAYMENT-SERVICE"))
            .build();
    }
}

