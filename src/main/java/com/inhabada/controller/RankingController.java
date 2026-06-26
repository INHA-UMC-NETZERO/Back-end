package com.inhabada.controller;

import com.inhabada.dto.CarbonRankingResponse;
import com.inhabada.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@Tag(name = "Ranking", description = "랭킹 API")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/carbon/monthly")
    @Operation(summary = "월간 탄소 저감량 랭킹 조회", description = "이번 달 완료된 나눔 요청 기준 사용자별 탄소 저감량 랭킹을 조회합니다.")
    public ResponseEntity<CarbonRankingResponse> getMonthlyCarbonRanking(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(rankingService.getMonthlyCarbonRanking(limit));
    }
}
