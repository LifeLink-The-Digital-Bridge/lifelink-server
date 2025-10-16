package com.matchingservice.utils;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DonorEventBuffer {
    private final Map<UUID, List<Runnable>> buffer = new ConcurrentHashMap<>();

    public void buffer(UUID donorId, Runnable handler) {
        buffer.computeIfAbsent(donorId, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }
    public List<Runnable> drain(UUID donorId) {
        return buffer.remove(donorId);
    }
}
