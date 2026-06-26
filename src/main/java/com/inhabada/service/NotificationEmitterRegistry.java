package com.inhabada.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 사용자별 SSE 연결을 메모리에 보관하고, 실시간 알림을 전송한다.
 * 단일 인스턴스 환경 전제 (다중 인스턴스로 확장 시 Redis pub/sub 등 별도 팬아웃 필요).
 */
@Component
public class NotificationEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(NotificationEmitterRegistry.class);

    // 30분 후 연결 종료 (클라이언트 EventSource가 자동 재연결)
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });
        emitter.onError(e -> remove(userId, emitter));

        // 연결 직후 핸드셰이크 이벤트 (프록시 버퍼링 방지 + 연결 확인용)
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            remove(userId, emitter);
        }

        return emitter;
    }

    public void send(Long userId, String eventName, Object data) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                log.debug("SSE 전송 실패, 연결 제거 (userId={})", userId);
                remove(userId, emitter);
            }
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
