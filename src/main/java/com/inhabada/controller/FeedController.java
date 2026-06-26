package com.inhabada.controller;

import com.inhabada.dto.PageResponse;
import com.inhabada.dto.PostCard;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostCard>> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String order) {
        Pageable pageable = PageRequest.of(pageNumber, size, resolveSort(order));
        Page<PostCard> result = postService.getActivePosts(category, keyword, pageable);
        return ResponseEntity.ok(PageResponse.from(result, card -> card));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    private Sort resolveSort(String order) {
        if ("oldest".equalsIgnoreCase(order)) {
            return Sort.by("createdAt").ascending();
        }
        return Sort.by("createdAt").descending();
    }
}
