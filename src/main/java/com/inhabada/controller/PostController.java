package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@CurrentUser Long userId,
                                                         @Valid @RequestBody CreatePostRequest request) {
        PostDetailResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closePost(@CurrentUser Long userId, @PathVariable Long id) {
        postService.closePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
