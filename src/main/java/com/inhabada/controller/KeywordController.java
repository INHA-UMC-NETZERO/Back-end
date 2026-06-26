package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.KeywordRequest;
import com.inhabada.dto.KeywordResponse;
import com.inhabada.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@Tag(name = "Keywords", description = "관심 키워드 구독 관리 API")
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping
    @Operation(summary = "내 키워드 목록 조회", description = "인증된 사용자가 등록한 관심 키워드 목록을 조회합니다.")
    public ResponseEntity<List<KeywordResponse>> getKeywords(@CurrentUser Long userId) {
        return ResponseEntity.ok(keywordService.getKeywords(userId));
    }

    @PostMapping
    @Operation(summary = "키워드 등록", description = "관심 키워드를 등록합니다. 새 게시글 제목/설명에 키워드가 포함되면 알림 후보가 됩니다.")
    public ResponseEntity<KeywordResponse> addKeyword(@CurrentUser Long userId,
                                                     @Valid @RequestBody KeywordRequest request) {
        KeywordResponse response = keywordService.addKeyword(userId, request.keyword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{keyword}")
    @Operation(summary = "키워드 삭제", description = "등록된 관심 키워드를 삭제합니다.")
    public ResponseEntity<Void> removeKeyword(@CurrentUser Long userId,
                                              @Parameter(description = "삭제할 키워드")
                                              @PathVariable String keyword) {
        keywordService.removeKeyword(userId, keyword);
        return ResponseEntity.noContent().build();
    }
}
