// api-gateway/src/main/java/com/gateway/apigateway/filter/AuthFilter.java
package com.gateway.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final String SECRET_KEY = "your-secret-key";

    private static final Set<String> ADMIN_ENDPOINTS = Set.of("/api/parking/lot", "/api/parking/lot/.*/spot");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (!isAuthorized(request.getRequestURI(), role)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Access denied for role: " + role);
                return;
            }

            request.setAttribute("X-User-Id", userId);
            request.setAttribute("X-User-Role", role);
            filterChain.doFilter(request, response);
        } catch (SignatureException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid token");
        }
    }

    private boolean isAuthorized(String uri, String role) {
        return ADMIN_ENDPOINTS.stream().noneMatch(uri::matches) || "ADMIN".equals(role);
    }
}

// api-gateway/src/main/java/com/gateway/apigateway/ApiGatewayApplication.java
package com.gateway.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableWebMvc
public class ApiGatewayApplication implements WebMvcConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8083"))
                .route("booking-service", r -> r.path("/api/booking/**")
                        .uri("http://localhost:8081"))
                .route("parking-service", r -> r.path("/api/parking/**")
                        .uri("http://localhost:8082"))
                .build();
    }
}

// api-gateway/pom.xml (dependencies section)
<!-- ... other dependencies ... -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

// api-gateway/src/main/resources/application.yml
spring:
  application:
    name: api-gateway

server:
  port: 8080

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    url: /v3/api-docs
    urls:
      - name: booking-service
        url: http://localhost:8081/v3/api-docs
      - name: parking-service
        url: http://localhost:8082/v3/api-docs
      - name: auth-service
        url: http://localhost:8083/v3/api-docs
