package com.inhabada.service;

import com.inhabada.dto.MatchResult;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.PostSimilarityProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticMatchingServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private EmbeddingClient embeddingClient;

    @Mock
    private ImageUrlResolver imageUrlResolver;

    private SemanticMatchingService semanticMatchingService;

    @BeforeEach
    void setUp() {
        semanticMatchingService = new SemanticMatchingService(
                postRepository,
                embeddingClient,
                imageUrlResolver,
                0.7,
                2,
                "https://inha-bada.app/"
        );
    }

    @Test
    void findMatchingPosts_usesVectorSearchAndFiltersBelowThresholdResults() {
        when(embeddingClient.embedQuery("cup ramen")).thenReturn("[0.1,0.2,0.3]");
        when(postRepository.searchSimilarByEmbedding("[0.1,0.2,0.3]", 0.7, 2))
                .thenReturn(List.of(
                        new SimilarPost(1L, "cup ramen share", "posts/ramen.jpg", 3, 0.82),
                        new SimilarPost(2L, "packing tape share", "posts/tape.jpg", 1, 0.62)
                ));
        when(imageUrlResolver.toUrl("posts/ramen.jpg"))
                .thenReturn("https://cdn.example.com/posts/ramen.jpg");

        List<MatchResult> results = semanticMatchingService.findMatchingPosts("cup ramen");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).postId()).isEqualTo(1L);
        assertThat(results.get(0).similarity()).isEqualTo(0.82);
        assertThat(results.get(0).webviewLink()).isEqualTo("https://inha-bada.app/posts/1");
        verify(embeddingClient).embedQuery("cup ramen");
        verify(postRepository).searchSimilarByEmbedding("[0.1,0.2,0.3]", 0.7, 2);
    }

    @Test
    void findMatchingPosts_returnsEmptyListWhenEmbeddingFails() {
        when(embeddingClient.embedQuery("cup ramen"))
                .thenThrow(new EmbeddingException("embedding failed"));

        List<MatchResult> results = semanticMatchingService.findMatchingPosts("cup ramen");

        assertThat(results).isEmpty();
        verify(embeddingClient).embedQuery("cup ramen");
        verify(postRepository, never()).searchSimilarByEmbedding(any(), anyDouble(), anyInt());
    }

    private record SimilarPost(
            Long id,
            String title,
            String thumbnailKey,
            Integer remainingQuantity,
            Double similarity
    ) implements PostSimilarityProjection {

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getThumbnailKey() {
            return thumbnailKey;
        }

        @Override
        public Integer getRemainingQuantity() {
            return remainingQuantity;
        }

        @Override
        public Double getSimilarity() {
            return similarity;
        }
    }
}
