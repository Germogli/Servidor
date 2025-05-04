package com.germogli.backend.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.germogli.backend.common.security.WebSocketSecurityInterceptor;

/**
 * Configuración definitiva para WebSockets con soporte para autenticación.
 * <p>
 * Esta configuración simplificada utiliza un único interceptor especializado
 * para garantizar que la autenticación esté disponible para los controladores.
 *
 * @author [Tu nombre]
 * @version 3.0
 * @since 2025-05-04
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketSecurityInterceptor webSocketSecurityInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {10000, 10000})
                .setTaskScheduler(taskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000);
    }

    /**
     * Configura solo el interceptor esencial para seguridad.
     * La simplicidad es clave - menos puntos de fallo.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registramos únicamente nuestro interceptor de seguridad especializado
        registration.interceptors(webSocketSecurityInterceptor);
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }
}