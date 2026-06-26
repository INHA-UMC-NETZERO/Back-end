package com.inhabada.controller;

import com.inhabada.dto.MatchResult;
import com.inhabada.dto.SemanticMatchRequest;
import com.inhabada.service.SemanticMatchingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final SemanticMatchingService semanticMatchingService;

    public MatchingController(SemanticMatchingService semanticMatchingService) {
        this.semanticMatchingService = semanticMatchingService;
    }

    @PostMapping("/semantic")
    public ResponseEntity<List<MatchResult>> match(@Valid @RequestBody SemanticMatchRequest request) {
        return ResponseEntity.ok(semanticMatchingService.findMatchingPosts(request.productName()));
    }
}
