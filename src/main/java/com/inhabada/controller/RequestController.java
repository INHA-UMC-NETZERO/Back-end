package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.CreateRequestDto;
import com.inhabada.dto.RequestResponse;
import com.inhabada.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Requests", description = "나눔 신청, 승인, 거절, 전달 완료 API")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/api/posts/{postId}/requests")
    @Operation(summary = "나눔 신청", description = "인증된 사용자가 특정 게시글에 나눔을 신청합니다. 생성된 요청은 신청중 상태로 시작합니다. 응답의 status는 프론트 표시용 한글 label로 반환됩니다.")
    public ResponseEntity<RequestResponse> createRequest(@CurrentUser Long userId,
                                                        @Parameter(description = "신청할 게시글 ID")
                                                        @PathVariable Long postId,
                                                        @Valid @RequestBody CreateRequestDto dto) {
        RequestResponse response = requestService.createRequest(userId, postId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/requests/{id}/approve")
    @Operation(summary = "신청 승인", description = "게시글 작성자가 신청중 요청을 승인합니다. 요청은 예약중 상태가 됩니다. 승인 후 남은 수량이 있으면 게시글은 나눔중 상태를 유지하고, 남은 수량이 0이면 예약중 상태가 됩니다.")
    public ResponseEntity<Void> approveRequest(@CurrentUser Long userId,
                                               @Parameter(description = "승인할 신청 ID")
                                               @PathVariable Long id) {
        requestService.approveRequest(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/requests/{id}/reject")
    @Operation(summary = "신청 거절", description = "게시글 작성자가 신청중 요청을 거절됨 상태로 변경합니다. status는 프론트 표시용 한글 label로 반환됩니다.")
    public ResponseEntity<Void> rejectRequest(@CurrentUser Long userId,
                                              @Parameter(description = "거절할 신청 ID")
                                              @PathVariable Long id) {
        requestService.rejectRequest(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/requests/{id}/complete")
    @Operation(summary = "전달 완료", description = "예약중 요청을 완료 상태로 변경하고 게시글을 마감 상태로 처리합니다. 프론트에는 신청중, 예약중, 완료, 거절됨 / 나눔중, 예약중, 마감 같은 한글 label이 반환됩니다.")
    public ResponseEntity<Void> completeRequest(@CurrentUser Long userId,
                                                @Parameter(description = "완료 처리할 신청 ID")
                                                @PathVariable Long id) {
        requestService.completeRequest(id, userId);
        return ResponseEntity.noContent().build();
    }
}
