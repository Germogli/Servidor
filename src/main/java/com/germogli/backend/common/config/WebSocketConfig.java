package com.germogli.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuración para habilitar y personalizar el uso de WebSockets con STOMP.
 * Se utiliza para permitir la comunicación en tiempo real en la aplicación.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes para la aplicación.
     * Se habilita un broker simple para destinos que comiencen con "/topic".
     * También se define un prefijo para los mensajes que los clientes envían al servidor.
     *
     * @param config Configuración del broker de mensajes.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple para destinos que comiencen con "/topic"
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {10000, 10000}) // 10 segundos para cliente y servidor
                .setTaskScheduler(taskScheduler()); // Programador de tareas para heartbeats

        // Establece el prefijo para mensajes que se envían desde el cliente hacia el servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra el endpoint que los clientes usarán para conectarse vía WebSocket.
     * Se configura con SockJS para mayor compatibilidad con navegadores que no soportan WebSocket.
     *
     * @param registry Registro de endpoints STOMP.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cambiar setAllowedOrigins por setAllowedOriginPatterns
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000); // 25 segundos (para detectar desconexiones rápidamente)
    }

    /**
     * Configura las opciones del cliente STOMP para la aplicación.
     *
     * @param registry Registro de opciones del cliente STOMP.
     */
    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor());
    }

    /**
     * Crea un interceptor para autenticar las conexiones WebSocket.
     *
     * @return Interceptor de autenticación
     */
    @Bean
    public WebSocketAuthInterceptor webSocketAuthInterceptor() {
        return new WebSocketAuthInterceptor();
    }

    /**
     * Configura el programador de tareas para los heartbeats.
     *
     * @return Programador de tareas
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }
}