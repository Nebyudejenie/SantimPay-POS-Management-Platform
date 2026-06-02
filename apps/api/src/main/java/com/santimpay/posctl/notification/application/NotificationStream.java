package com.santimpay.posctl.notification.application;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * In-memory SSE fan-out for live in-app notifications. Keyed by recipient id; a null-recipient
 * (broadcast) push is delivered to all connected emitters. For a single-node baseline this is
 * sufficient; when the API scales horizontally, back this with a Redis pub/sub fan-out so an event
 * on one pod reaches SSE clients on another.
 */
@Slf4j
@Component
public class NotificationStream {

    private final Map<UUID, List<SseEmitter>> byUser = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        byUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        return emitter;
    }

    public void push(UUID recipientId, String eventName, Object data) {
        if (recipientId == null) {
            byUser.keySet().forEach(u -> send(u, eventName, data));
        } else {
            send(recipientId, eventName, data);
        }
    }

    private void send(UUID userId, String eventName, Object data) {
        List<SseEmitter> emitters = byUser.getOrDefault(userId, List.of());
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException ex) {
                remove(userId, e);
            }
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        List<SseEmitter> emitters = byUser.get(userId);
        if (emitters != null) emitters.remove(emitter);
    }
}
