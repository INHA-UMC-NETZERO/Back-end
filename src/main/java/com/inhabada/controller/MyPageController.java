package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.MyPageSummaryResponse;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@Tag(name = "MyPage", description = "마이페이지 프로필, 활동 요약, 내 게시글, 내 신청 내역 조회 API")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/posts")
    @Operation(summary = "내 게시글 목록 조회", description = "인증된 사용자가 등록한 게시글, 신청 목록, 단위 탄소 저감량, 완료된 탄소 저감량을 조회합니다.")
    public ResponseEntity<List<MyPostResponse>> getMyPosts(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyPosts(userId));
    }

    @GetMapping("/summary")
    @Operation(summary = "마이페이지 요약 조회", description = "프로필, 활동 요약, 누적 탄소 저감량, 최근 6개월 월별 탄소 저감량을 조회합니다. 탄소 저감량 단위는 gram입니다.")
    public ResponseEntity<MyPageSummaryResponse> getSummary(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getSummary(userId));
    }

    @GetMapping("/requests")
    @Operation(summary = "내 신청 목록 조회", description = "인증된 사용자가 다른 게시글에 신청한 요청과 완료 시 적립된 탄소 저감량을 조회합니다. 완료 전 요청의 carbonSavingGram과 completedAt은 null입니다.")
    public ResponseEntity<List<MyRequestResponse>> getMyRequests(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyRequests(userId));
    }
}
