package com.inhabada.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "서버 상태 확인 API")
public class HealthController {

    @GetMapping
    @Operation(summary = "헬스체크", description = "서버가 정상적으로 응답 가능한 상태인지 확인합니다.")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
