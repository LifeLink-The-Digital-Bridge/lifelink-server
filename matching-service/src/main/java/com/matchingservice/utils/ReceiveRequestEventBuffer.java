package com.matchingservice.utils;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReceiveRequestEventBuffer {
    private final Map<String, List<Runnable>> buffer = new ConcurrentHashMap<>();

    private String key(UUID recipientId, UUID locationId) {
        return recipientId + ":" + locationId;
    }

    public void buffer(UUID recipientId, UUID locationId, Runnable handler) {
        buffer.computeIfAbsent(key(recipientId, locationId), k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }

    public List<Runnable> drain(UUID recipientId, UUID locationId) {
        return buffer.remove(key(recipientId, locationId));
    }
}
