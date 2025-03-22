package com.germogli.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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
        config.enableSimpleBroker("/topic");
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
        // Registra el endpoint "/ws" y permite solicitudes desde cualquier origen
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
