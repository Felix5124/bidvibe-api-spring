package com.bidvibe.bidvibeapispring.config;

import com.bidvibe.bidvibeapispring.constant.SecurityConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket STOMP cho BidVibe.
 * - Endpoint kết nối: /ws (SockJS fallback)
 * - App destination prefix: /app
 * - Broker topics: /topic (broadcast), /queue (user-specific)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các message từ client gửi lên controller (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix broker: /topic (broadcast nhiều người), /queue (gửi riêng 1 người)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho gửi riêng tới user (/user/{userId}/queue/...)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SecurityConstants.WS_ENDPOINT)
                .setAllowedOrigins(SecurityConstants.ALLOWED_ORIGINS)
                .withSockJS();
    }
}

