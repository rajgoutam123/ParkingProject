1 ..


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

        // Add user details to headers
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader("X-User-Id", userId);
        mutableRequest.putHeader("X-User-Role", role);

        filterChain.doFilter(mutableRequest, response); // Forward modified request
    } catch (SignatureException e) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("Invalid token");
    }
}


2..


modity header in erquest

package com.gateway.apigateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {
    private final Map<String, String> customHeaders;

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }

    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);
        return (headerValue != null) ? headerValue : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(customHeaders.keySet());
        Enumeration<String> originalNames = super.getHeaderNames();
        while (originalNames.hasMoreElements()) {
            names.add(originalNames.nextElement());
        }
        return Collections.enumeration(names);
    }
}


3..


@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            .route("auth-service", r -> r.path("/api/auth/**")
                    .filters(f -> f.addRequestHeader("X-User-Id", "${X-User-Id}")
                                 .addRequestHeader("X-User-Role", "${X-User-Role}"))
                    .uri("lb://auth-service")) // "lb://" = Load-balanced via Eureka
            .route("booking-service", r -> r.path("/api/booking/**")
                    .filters(f -> f.addRequestHeader("X-User-Id", "${X-User-Id}")
                                 .addRequestHeader("X-User-Role", "${X-User-Role}"))
                    .uri("lb://booking-service"))
            .route("parking-service", r -> r.path("/api/parking/**")
                    .filters(f -> f.addRequestHeader("X-User-Id", "${X-User-Id}")
                                 .addRequestHeader("X-User-Role", "${X-User-Role}"))
                    .uri("lb://parking-service"))
            .build();
}
