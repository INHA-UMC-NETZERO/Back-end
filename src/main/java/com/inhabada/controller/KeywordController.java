package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.KeywordRequest;
import com.inhabada.dto.KeywordResponse;
import com.inhabada.service.KeywordService;
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
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping
    public ResponseEntity<List<KeywordResponse>> getKeywords(@CurrentUser Long userId) {
        return ResponseEntity.ok(keywordService.getKeywords(userId));
    }

    @PostMapping
    public ResponseEntity<KeywordResponse> addKeyword(@CurrentUser Long userId,
                                                     @Valid @RequestBody KeywordRequest request) {
        KeywordResponse response = keywordService.addKeyword(userId, request.keyword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{keyword}")
    public ResponseEntity<Void> removeKeyword(@CurrentUser Long userId, @PathVariable String keyword) {
        keywordService.removeKeyword(userId, keyword);
        return ResponseEntity.noContent().build();
    }
}
