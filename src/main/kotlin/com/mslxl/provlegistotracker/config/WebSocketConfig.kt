package com.mslxl.provlegistotracker.config

import com.mslxl.provlegistotracker.ws.WebSocketSubscribeHandler
import com.mslxl.provlegistotracker.ws.WebSocketSubscribeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig(
    val subscribeHandler: WebSocketSubscribeHandler,
    val subscribeInterceptor: WebSocketSubscribeInterceptor
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(subscribeHandler, "/session/{id}")
            .addInterceptors(HttpSessionHandshakeInterceptor())
            .addInterceptors(subscribeInterceptor)
            .setAllowedOrigins("*")

    }


}