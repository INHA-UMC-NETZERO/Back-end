package com.inhabada.event;

import com.inhabada.entity.Category;
import com.inhabada.entity.KeywordSubscription;
import com.inhabada.entity.NotificationType;
import com.inhabada.entity.Post;
import com.inhabada.entity.SubCategory;
import com.inhabada.repository.KeywordSubscriptionRepository;
import com.inhabada.repository.ShareRequestRepository;
import com.inhabada.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private KeywordSubscriptionRepository keywordSubscriptionRepository;

    @Mock
    private ShareRequestRepository shareRequestRepository;

    @Mock
    private NotificationService notificationService;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(
                keywordSubscriptionRepository,
                shareRequestRepository,
                notificationService,
                0.7
        );
    }

    @Test
    void handlePostCreated_notifiesVectorMatchedKeywordSubscribers() {
        Post post = new Post(
                10L,
                "컵라면 나눔",
                "행사 후 남은 물품입니다",
                Category.FOOD,
                SubCategory.CUP_RAMEN,
                new String[]{"posts/ramen.jpg"},
                3,
                "5호관 로비",
                "금요일 오후"
        );
        ReflectionTestUtils.setField(post, "id", 100L);
        KeywordSubscription subscription = new KeywordSubscription(20L, "라면");

        when(keywordSubscriptionRepository.findSimilarByPostEmbedding(100L, 10L, 0.7))
                .thenReturn(List.of(subscription));

        listener.handlePostCreated(new PostCreatedEvent(post));

        verify(keywordSubscriptionRepository).findSimilarByPostEmbedding(100L, 10L, 0.7);
        verify(keywordSubscriptionRepository, never()).findAll();
        verify(notificationService).createNotification(
                20L,
                NotificationType.KEYWORD_MATCH,
                "관심 키워드 '라면'와 유사한 새 게시글이 등록되었습니다: 컵라면 나눔",
                100L
        );
    }
}
