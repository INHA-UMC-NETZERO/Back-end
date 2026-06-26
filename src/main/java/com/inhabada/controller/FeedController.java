package com.inhabada.controller;

import com.inhabada.dto.PageResponse;
import com.inhabada.dto.PostCard;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.exception.ValidationException;
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

import java.util.List;

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
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String order) {
        validatePageRequest(pageNumber, size);
        Pageable pageable = PageRequest.of(pageNumber, size, resolveSort(order));
        Page<PostCard> result = postService.getActivePosts(category, keyword, pageable);
        return ResponseEntity.ok(PageResponse.from(result, card -> card));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    private void validatePageRequest(int pageNumber, int size) {
        if (pageNumber < 0) {
            throw new ValidationException("page는 0 이상이어야 합니다", List.of("page"));
        }
        if (size < 1 || size > 100) {
            throw new ValidationException("size는 1 이상 100 이하여야 합니다", List.of("size"));
        }
    }

    private Sort resolveSort(String order) {
        if ("oldest".equalsIgnoreCase(order)) {
            return Sort.by("createdAt").ascending();
        }
        if (!"latest".equalsIgnoreCase(order)) {
            throw new ValidationException("order는 latest 또는 oldest만 사용할 수 있습니다", List.of("order"));
        }
        return Sort.by("createdAt").descending();
    }
}
