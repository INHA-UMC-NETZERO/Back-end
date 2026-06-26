package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<MyPostResponse>> getMyPosts(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyPosts(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<MyRequestResponse>> getMyRequests(@CurrentUser Long userId) {
        return ResponseEntity.ok(myPageService.getMyRequests(userId));
    }
}
