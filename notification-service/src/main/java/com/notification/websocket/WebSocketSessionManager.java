package com.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<UUID, String> userSessions = new ConcurrentHashMap<>();
    
    private final Map<String, UUID> sessionUsers = new ConcurrentHashMap<>();

    public void addSession(UUID userId, String sessionId) {
        userSessions.put(userId, sessionId);
        sessionUsers.put(sessionId, userId);
        log.info("WebSocket session added - UserId: {}, SessionId: {}", userId, sessionId);
        log.info("Total active sessions: {}", userSessions.size());
    }

    public void removeSession(String sessionId) {
        UUID userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket session removed - UserId: {}, SessionId: {}", userId, sessionId);
        }
        log.info("Total active sessions: {}", userSessions.size());
    }

    public boolean isUserConnected(UUID userId) {
        return userSessions.containsKey(userId);
    }

    public String getSessionId(UUID userId) {
        return userSessions.get(userId);
    }

    public UUID getUserId(String sessionId) {
        return sessionUsers.get(sessionId);
    }

    public int getActiveSessionCount() {
        return userSessions.size();
    }
}
