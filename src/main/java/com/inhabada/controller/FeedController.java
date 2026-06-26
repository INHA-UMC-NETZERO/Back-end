package com.inhabada.controller;

import com.inhabada.dto.PageResponse;
import com.inhabada.dto.PostCard;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.exception.ValidationException;
import com.inhabada.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Feed", description = "나눔 게시글 목록과 상세 조회 API")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(
            summary = "게시글 목록 조회",
            description = "신청 가능한 게시글 목록을 페이지 단위로 조회합니다. category는 FOOD, DRINK, STATIONERY_EVENT, PACKING_ORGANIZING, FURNITURE_SPACE, ETC 같은 enum code를 사용하고, keyword는 제목/설명 검색에 사용합니다."
    )
    public ResponseEntity<PageResponse<PostCard>> getPosts(
            @Parameter(description = "카테고리 enum code. 예: FOOD, DRINK, STATIONERY_EVENT, PACKING_ORGANIZING, FURNITURE_SPACE, ETC")
            @RequestParam(required = false) String category,
            @Parameter(description = "제목 또는 설명 검색어")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "0부터 시작하는 페이지 번호")
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @Parameter(description = "페이지 크기. 1 이상 100 이하")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(description = "정렬 순서. latest 또는 oldest")
            @RequestParam(defaultValue = "latest") String order) {
        validatePageRequest(pageNumber, size);
        Pageable pageable = PageRequest.of(pageNumber, size, resolveSort(order));
        Page<PostCard> result = postService.getActivePosts(category, keyword, pageable);
        return ResponseEntity.ok(PageResponse.from(result, card -> card));
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 제목, 설명, 이미지, 수량, 위치, 카테고리 label 등을 포함한 상세 정보를 조회합니다.")
    public ResponseEntity<PostDetailResponse> getPost(
            @Parameter(description = "조회할 게시글 ID")
            @PathVariable Long id) {
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
