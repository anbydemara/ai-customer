package com.young.aicustomer.config;

import com.young.aicustomer.handler.QAWebSocketHandler;
import com.young.aicustomer.interceptor.SocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final QAWebSocketHandler socketHandler;
    private final SocketAuthInterceptor socketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(socketHandler, "/chat")
                .addInterceptors(socketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
