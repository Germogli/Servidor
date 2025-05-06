package com.germogli.backend.common.diagnostic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para diagnóstico de problemas de autenticación WebSocket.
 * Útil para verificar el estado de la autenticación sin interactuar con la BD.
 */
@Controller
@Slf4j
public class WebSocketDiagnosticController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Endpoint de diagnóstico para verificar la autenticación.
     */
    @MessageMapping("/diagnostic/auth-check")
    public void checkAuthentication(SimpMessageHeaderAccessor headerAccessor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "No autenticado";
        boolean isAuthenticated = auth != null && auth.isAuthenticated();

        log.info("Diagnóstico de autenticación: usuario={}, autenticado={}",
                username, isAuthenticated);

        if (headerAccessor.getUser() != null) {
            String userFromHeader = headerAccessor.getUser().getName();
            log.info("Usuario en header: {}", userFromHeader);

            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("authenticated", isAuthenticated);
            result.put("headerUsername", userFromHeader);
            result.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/queue/diagnostic-result",
                    result
            );
        }
    }
}