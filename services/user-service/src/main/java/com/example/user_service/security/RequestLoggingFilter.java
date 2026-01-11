package com.example.user_service.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.info("=== REQUEST: {} {} ===", httpRequest.getMethod(), httpRequest.getRequestURI());
        Collections.list(httpRequest.getHeaderNames()).forEach(h ->
                log.info("Header: {} = {}", h, httpRequest.getHeader(h))
        );
        chain.doFilter(request, response);
    }
}