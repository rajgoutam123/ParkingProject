
package com.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService; // This is a service to validate token, or do manually here

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.isAuthenticationRequired()) {
                // Check if Authorization header is present
                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
                }
                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
                }
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey("secret_key") // SAME secret key as used in AuthService
                            .parseClaimsJws(token)
                            .getBody();
                    // You can add claims to request if needed
                } catch (Exception e) {
                    return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        byte[] bytes = err.getBytes();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        private boolean authenticationRequired = true;

        public boolean isAuthenticationRequired() {
            return authenticationRequired;
        }

        public Config setAuthenticationRequired(boolean authenticationRequired) {
            this.authenticationRequired = authenticationRequired;
            return this;
        }
    }
}
