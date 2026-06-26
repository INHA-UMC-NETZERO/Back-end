package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.CreateRequestDto;
import com.inhabada.dto.RequestResponse;
import com.inhabada.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/api/posts/{postId}/requests")
    public ResponseEntity<RequestResponse> createRequest(@CurrentUser Long userId,
                                                        @PathVariable Long postId,
                                                        @Valid @RequestBody CreateRequestDto dto) {
        RequestResponse response = requestService.createRequest(userId, postId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/requests/{id}/approve")
    public ResponseEntity<Void> approveRequest(@CurrentUser Long userId, @PathVariable Long id) {
        requestService.approveRequest(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@CurrentUser Long userId, @PathVariable Long id) {
        requestService.rejectRequest(id, userId);
        return ResponseEntity.noContent().build();
    }
}
