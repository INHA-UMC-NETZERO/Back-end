package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
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
@Tag(name = "MyPage", description = "내가 등록한 게시글과 내가 신청한 내역 조회 API")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/posts")
    @Operation(summary = "내 게시글 목록 조회", description = "인증된 사용자가 등록한 게시글과 각 게시글의 category/subCategory/location, 신청 목록을 조회합니다. category/subCategory/status는 프론트 표시용 한글 label로 반환됩니다.")
    public ResponseEntity<List<MyPostResponse>> getMyPosts(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyPosts(userId));
    }

    @GetMapping("/requests")
    @Operation(summary = "내 신청 목록 조회", description = "인증된 사용자가 다른 게시글에 신청한 요청과 신청한 게시글의 category/subCategory/location을 조회합니다. category/subCategory/status는 프론트 표시용 한글 label로 반환됩니다.")
    public ResponseEntity<List<MyRequestResponse>> getMyRequests(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyRequests(userId));
    }
}
