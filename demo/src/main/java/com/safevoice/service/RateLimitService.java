package com.safevoice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60 * 1000;
    private static final int MAX_REQUESTS = 5;

    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        requestTimestamps.putIfAbsent(clientIp, new ArrayList<>());
        List<Long> timestamps = requestTimestamps.get(clientIp);
        synchronized (timestamps) {
            Iterator<Long> it = timestamps.iterator();
            while (it.hasNext()) {
                if (now - it.next() > WINDOW_MS) {
                    it.remove();
                } else {
                    break;
                }
            }
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }
}