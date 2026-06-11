package com.bachelor.sensorsmonitoringservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Slf4j
@Service
public class SseTelemetryService {

    // sensorId -> list of active emitters
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long sensorId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(sensorId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public void push(Long sensorId, double value, String timestamp, String status) {
        List<SseEmitter> list = emitters.get(sensorId);
        if (list == null || list.isEmpty()) return;

        String data = String.format("{\"value\":%.4f,\"timestamp\":\"%s\",\"status\":\"%s\"}", value, timestamp, status);
        list.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(data));
                return false;
            } catch (IOException e) {
                return true; // remove dead emitter
            }
        });
    }

    private void removeEmitter(Long sensorId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(sensorId);
        if (list != null) list.remove(emitter);
    }
}
