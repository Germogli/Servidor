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

/**
 * Configuración para habilitar y personalizar el uso de WebSockets con STOMP.
 * Se utiliza para permitir la comunicación en tiempo real en la aplicación.
 * <p>
 * Esta versión mejorada incluye un interceptor para propagar la autenticación
 * al SecurityContext durante el procesamiento de mensajes, resolviendo problemas
 * con anotaciones @PreAuthorize en los controladores de mensajería.
 *
 * @author [Tu nombre]
 * @version 1.1
 * @since 2025-05-04
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Autowired
    private AuthenticationChannelInterceptor authenticationChannelInterceptor;

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
        config.enableSimpleBroker("/topic", "/queue")
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
     * Configura las opciones del cliente STOMP para la aplicación, incluyendo
     * los interceptores para autenticación y propagación del contexto de seguridad.
     *
     * @param registration Registro de opciones del canal de entrada STOMP.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // El orden es importante: AuthenticationChannelInterceptor debe ejecutarse después
        // de WebSocketAuthInterceptor para usar la autenticación establecida
        registration.interceptors(webSocketAuthInterceptor);
        registration.interceptors(authenticationChannelInterceptor);
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