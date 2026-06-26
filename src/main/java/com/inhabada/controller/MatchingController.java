package com.inhabada.controller;

import com.inhabada.dto.MatchResult;
import com.inhabada.dto.SemanticMatchRequest;
import com.inhabada.service.SemanticMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@Tag(name = "Matching", description = "상품명 기반 나눔 게시글 추천 API")
public class MatchingController {

    private final SemanticMatchingService semanticMatchingService;

    public MatchingController(SemanticMatchingService semanticMatchingService) {
        this.semanticMatchingService = semanticMatchingService;
    }

    @PostMapping("/semantic")
    @Operation(
            summary = "유사 게시글 추천",
            description = "입력한 상품명을 임베딩 서버로 벡터화한 뒤 posts.embedding과 cosine similarity 기준으로 유사한 ACTIVE 게시글을 반환합니다."
    )
    public ResponseEntity<List<MatchResult>> match(@Valid @RequestBody SemanticMatchRequest request) {
        return ResponseEntity.ok(semanticMatchingService.findMatchingPosts(request.productName()));
    }
}
