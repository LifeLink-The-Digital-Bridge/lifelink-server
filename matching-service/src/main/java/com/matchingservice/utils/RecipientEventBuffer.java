package com.matchingservice.utils;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecipientEventBuffer {
    private final Map<UUID, List<Runnable>> buffer = new ConcurrentHashMap<>();

    public void buffer(UUID recipientId, Runnable handler) {
        buffer.computeIfAbsent(recipientId, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }

    public List<Runnable> drain(UUID recipientId) {
        return buffer.remove(recipientId);
    }
}
