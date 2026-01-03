//package com.interview_platform.call_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//class WebSocketSecurityConfig {
//
//    // Allow WebSocket connections
//    // In production, add proper JWT validation
//    @Bean
//    public org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
//    webSocketSecurity() {
//        return new org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer() {
//            @Override
//            protected void configureInbound(
//                    org.springframework.security.config.annotation.web.socket.MessageSecurityMetadataSourceRegistry messages) {
//                messages.anyMessage().permitAll();
//            }
//
//            @Override
//            protected boolean sameOriginDisabled() {
//                return true;
//            }
//        };
//    }
//}
