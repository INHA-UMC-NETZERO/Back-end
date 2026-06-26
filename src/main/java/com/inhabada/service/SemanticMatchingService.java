package com.inhabada.service;

import com.inhabada.dto.MatchResult;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.PostSimilarityProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SemanticMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SemanticMatchingService.class);

    private final PostRepository postRepository;
    private final EmbeddingClient embeddingClient;
    private final ImageUrlResolver imageUrlResolver;
    private final double similarityThreshold;
    private final int maxResults;
    private final String webviewBaseUrl;

    public SemanticMatchingService(PostRepository postRepository,
                                   EmbeddingClient embeddingClient,
                                   ImageUrlResolver imageUrlResolver,
                                   @Value("${app.matching.similarity-threshold:0.7}") double similarityThreshold,
                                   @Value("${app.matching.max-results:5}") int maxResults,
                                   @Value("${app.webview.base-url}") String webviewBaseUrl) {
        this.postRepository = postRepository;
        this.embeddingClient = embeddingClient;
        this.imageUrlResolver = imageUrlResolver;
        this.similarityThreshold = similarityThreshold;
        this.maxResults = maxResults;
        this.webviewBaseUrl = webviewBaseUrl.endsWith("/")
                ? webviewBaseUrl.substring(0, webviewBaseUrl.length() - 1)
                : webviewBaseUrl;
    }

    @Transactional(readOnly = true)
    public List<MatchResult> findMatchingPosts(String productName) {
        try {
            String embedding = embeddingClient.embedQuery(productName);
            return postRepository.searchSimilarByEmbedding(embedding, similarityThreshold, maxResults).stream()
                    .filter(result -> result.getSimilarity() != null)
                    .filter(result -> result.getSimilarity() >= similarityThreshold)
                    .sorted(Comparator.comparingDouble(PostSimilarityProjection::getSimilarity).reversed())
                    .limit(maxResults)
                    .map(this::toMatchResult)
                    .toList();
        } catch (Exception ex) {
            log.warn("Semantic matching failed, returning empty result: {}", ex.getMessage());
            return List.of();
        }
    }

    private MatchResult toMatchResult(PostSimilarityProjection result) {
        return new MatchResult(
                result.getId(),
                result.getTitle(),
                imageUrlResolver.toUrl(result.getThumbnailKey()),
                result.getRemainingQuantity(),
                webviewBaseUrl + "/posts/" + result.getId(),
                result.getSimilarity()
        );
    }
}
