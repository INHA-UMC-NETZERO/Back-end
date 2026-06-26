package com.inhabada.service;

import com.inhabada.dto.KeywordResponse;
import com.inhabada.entity.KeywordSubscription;
import com.inhabada.repository.KeywordSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock
    private KeywordSubscriptionRepository keywordSubscriptionRepository;

    @Mock
    private EmbeddingClient embeddingClient;

    private KeywordService keywordService;

    @BeforeEach
    void setUp() {
        keywordService = new KeywordService(keywordSubscriptionRepository, embeddingClient);
    }

    @Test
    void addKeyword_storesEmbeddingForNormalizedKeyword() {
        when(keywordSubscriptionRepository.existsByUserIdAndKeyword(1L, "컵라면")).thenReturn(false);
        when(keywordSubscriptionRepository.countByUserId(1L)).thenReturn(0);
        when(embeddingClient.embedQuery("컵라면")).thenReturn("[0.1,0.2,0.3]");
        when(keywordSubscriptionRepository.save(any(KeywordSubscription.class))).thenAnswer(invocation -> {
            KeywordSubscription subscription = invocation.getArgument(0);
            ReflectionTestUtils.setField(subscription, "id", 10L);
            return subscription;
        });

        KeywordResponse response = keywordService.addKeyword(1L, " 컵라면 ");

        assertThat(response.keyword()).isEqualTo("컵라면");
        verify(embeddingClient).embedQuery("컵라면");
        verify(keywordSubscriptionRepository).updateEmbedding(10L, "[0.1,0.2,0.3]");
    }
}
