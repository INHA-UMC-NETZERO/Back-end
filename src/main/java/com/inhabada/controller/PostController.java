package com.inhabada.controller;

import com.inhabada.config.CurrentUser;
import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.service.PostService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "나눔 게시글 등록과 상태 변경 API")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(
            summary = "게시글 등록",
            description = "인증된 사용자가 나눔 게시글을 등록합니다. category/subCategory는 식품, 과자 같은 한글 label을 사용합니다. 기타는 subCategory 없이 등록할 수 있고, 그 외 카테고리는 해당 카테고리에 속한 subCategory가 필요합니다. 기존 영어 enum code도 호환됩니다."
    )
    public ResponseEntity<PostDetailResponse> createPost(@CurrentUser Long userId,
                                                         @Valid @RequestBody CreatePostRequest request) {
        PostDetailResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "게시글 마감", description = "게시글 작성자가 게시글을 직접 마감합니다. 마감된 게시글은 더 이상 신청을 받을 수 없습니다.")
    public ResponseEntity<Void> closePost(@CurrentUser Long userId,
                                          @Parameter(description = "마감할 게시글 ID")
                                          @PathVariable Long id) {
        postService.closePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
