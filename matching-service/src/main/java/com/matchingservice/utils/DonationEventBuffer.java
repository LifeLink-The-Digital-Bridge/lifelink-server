package com.matchingservice.utils;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DonationEventBuffer {
    private final Map<String, List<Runnable>> buffer = new ConcurrentHashMap<>();
    private String key(UUID donorId, Long locationId) {
        return donorId + ":" + locationId;
    }
    public void buffer(UUID donorId, Long locationId, Runnable handler) {
        buffer.computeIfAbsent(key(donorId, locationId), k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }
    public List<Runnable> drain(UUID donorId, Long locationId) {
        return buffer.remove(key(donorId, locationId));
    }
}
