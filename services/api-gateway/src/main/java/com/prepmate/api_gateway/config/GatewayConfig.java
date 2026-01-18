package com.prepmate.api_gateway.config;

import com.prepmate.api_gateway.security.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig
{
    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Public routes - No authentication
                .route("user-service-auth-login", r -> r.path("/prep-mate/api/auth/login")
                        .filters(f -> f.rewritePath("/prep-mate/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("lb://user-service"))

                .route("user-service-auth-register", r -> r.path("/prep-mate/api/auth/register")
                        .filters(f -> f.rewritePath("/prep-mate/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("lb://user-service"))

                // Protected routes - Authentication required
                .route("user-service-protected", r -> r.path("/prep-mate/api/users/**")
                        .filters(f -> f
                                .rewritePath("/prep-mate/api/(?<segment>.*)", "/api/${segment}")
                                .filter(filter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))

                .route("interview-service-protected", r -> r.path("/prep-mate/api/interviews/**")
                        .filters(f -> f
                                .rewritePath("/prep-mate/api/(?<segment>.*)", "/api/${segment}")
                                .filter(filter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://interview-service"))

                // Add more protected routes for other services as needed
                .build();
    }
}
