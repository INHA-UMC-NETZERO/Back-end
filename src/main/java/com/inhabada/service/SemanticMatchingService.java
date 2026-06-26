package com.inhabada.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inhabada.dto.MatchResult;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SemanticMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SemanticMatchingService.class);
    private static final int CANDIDATE_LIMIT = 50;

    private final PostRepository postRepository;
    private final BedrockClient bedrockClient;
    private final ImageUrlResolver imageUrlResolver;
    private final ObjectMapper objectMapper;
    private final double similarityThreshold;
    private final int maxResults;
    private final String webviewBaseUrl;

    public SemanticMatchingService(PostRepository postRepository,
                                   BedrockClient bedrockClient,
                                   ImageUrlResolver imageUrlResolver,
                                   ObjectMapper objectMapper,
                                   @Value("${app.matching.similarity-threshold:0.7}") double similarityThreshold,
                                   @Value("${app.matching.max-results:5}") int maxResults,
                                   @Value("${app.webview.base-url}") String webviewBaseUrl) {
        this.postRepository = postRepository;
        this.bedrockClient = bedrockClient;
        this.imageUrlResolver = imageUrlResolver;
        this.objectMapper = objectMapper;
        this.similarityThreshold = similarityThreshold;
        this.maxResults = maxResults;
        this.webviewBaseUrl = webviewBaseUrl.endsWith("/")
                ? webviewBaseUrl.substring(0, webviewBaseUrl.length() - 1)
                : webviewBaseUrl;
    }

    @Transactional(readOnly = true)
    public List<MatchResult> findMatchingPosts(String productName) {
        List<Post> candidates = postRepository
                .findByStatus(PostStatus.ACTIVE,
                        PageRequest.of(0, CANDIDATE_LIMIT, Sort.by("createdAt").descending()))
                .getContent();

        if (candidates.isEmpty()) {
            return List.of();
        }

        try {
            String prompt = buildPrompt(productName, candidates);
            String responseText = bedrockClient.converse(prompt);
            return parseResults(responseText, candidates);
        } catch (Exception ex) {
            // Timeout 등 오류 시 빈 결과 반환 (Requirement 8.7 흐름은 클라이언트에서 처리)
            log.warn("Semantic matching failed, returning empty result: {}", ex.getMessage());
            return List.of();
        }
    }

    private String buildPrompt(String productName, List<Post> candidates) throws Exception {
        ArrayNode postsArray = objectMapper.createArrayNode();
        for (Post post : candidates) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", post.getId());
            node.put("title", post.getTitle());
            node.put("description", post.getDescription());
            postsArray.add(node);
        }
        String postsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(postsArray);

        return """
                주어진 쿠팡 상품명과 아래 나눔 게시글 목록을 비교하여, 의미적으로 유사한 게시글을 찾아주세요.
                유사도(similarity)는 0.0~1.0 사이의 값이며, 0.7 이상인 항목만 포함하세요.
                반드시 JSON 배열만 출력하고 다른 설명은 포함하지 마세요.

                상품명: "%s"

                게시글 목록:
                %s

                응답 형식: [{"postId": <id>, "similarity": <0.0~1.0>, "reason": "<간단한 이유>"}]
                """.formatted(productName, postsJson);
    }

    private List<MatchResult> parseResults(String responseText, List<Post> candidates) throws Exception {
        Map<Long, Post> postById = candidates.stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        String json = extractJsonArray(responseText);
        if (json == null) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(json);
        if (!root.isArray()) {
            return List.of();
        }

        List<MatchResult> results = new ArrayList<>();
        for (JsonNode node : root) {
            if (!node.hasNonNull("postId") || !node.hasNonNull("similarity")) {
                continue;
            }
            double similarity = node.get("similarity").asDouble();
            if (similarity < similarityThreshold) {
                continue;
            }
            Long postId = node.get("postId").asLong();
            Post post = postById.get(postId);
            if (post == null) {
                continue;
            }
            results.add(new MatchResult(
                    post.getId(),
                    post.getTitle(),
                    imageUrlResolver.firstUrl(post.getImageKeys()),
                    post.getRemainingQuantity(),
                    webviewBaseUrl + "/posts/" + post.getId(),
                    similarity
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(MatchResult::similarity).reversed())
                .limit(maxResults)
                .toList();
    }

    private String extractJsonArray(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end < 0 || end < start) {
            return null;
        }
        return text.substring(start, end + 1);
    }
}
