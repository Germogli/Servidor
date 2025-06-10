package com.germogli.backend.common.config;

import com.germogli.backend.common.security.WebSocketHandshakeInterceptor;
import com.germogli.backend.common.security.WebSocketSessionSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Configuración optimizada de WebSockets con autenticación por cookies JWT.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketSessionSecurityInterceptor sessionSecurityInterceptor;

    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Configurar broker de mensajes con heartbeat optimizado
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {10000, 10000})
                .setTaskScheduler(taskScheduler());

        // Prefijos para destinos de aplicación
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes dirigidos a usuarios específicos
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP con autenticación por cookies
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Usar patterns para desarrollo
                .addInterceptors(handshakeInterceptor) // ✅ AGREGAR interceptor de handshake
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(30000)
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(true) // ✅ CAMBIAR a true para cookies
                .setSuppressCors(false);
    }

    /**
     * Configuración de interceptores para canal de entrada.
     * IMPORTANTE: Usar el nuevo interceptor de sesión en lugar del anterior.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // ✅ USAR el nuevo interceptor de sesión
        registration.interceptors(sessionSecurityInterceptor);

        // Configurar pool de hilos para mejor rendimiento
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10);
    }

    /**
     * Aumentar límites de tamaño de mensaje para tokens JWT grandes.
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(128 * 1024); // 128KB
        registry.setSendBufferSizeLimit(512 * 1024); // 512KB
        registry.setSendTimeLimit(20000); // 20 segundos
    }

    /**
     * Scheduler dedicado para tareas WebSocket.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

    /**
     * Configuración optimizada del contenedor WebSocket.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024); // 64KB
        container.setMaxBinaryMessageBufferSize(64 * 1024); // 64KB
        container.setMaxSessionIdleTimeout(120000L); // 2 minutos
        return container;
    }
}