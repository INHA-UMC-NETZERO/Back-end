package com.inhabada.event;

import com.inhabada.entity.KeywordSubscription;
import com.inhabada.entity.NotificationType;
import com.inhabada.entity.Post;
import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import com.inhabada.repository.KeywordSubscriptionRepository;
import com.inhabada.repository.ShareRequestRepository;
import com.inhabada.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final KeywordSubscriptionRepository keywordSubscriptionRepository;
    private final ShareRequestRepository shareRequestRepository;
    private final NotificationService notificationService;
    private final double keywordSimilarityThreshold;

    public NotificationEventListener(KeywordSubscriptionRepository keywordSubscriptionRepository,
                                     ShareRequestRepository shareRequestRepository,
                                     NotificationService notificationService,
                                     @Value("${app.keyword-matching.similarity-threshold:0.7}")
                                     double keywordSimilarityThreshold) {
        this.keywordSubscriptionRepository = keywordSubscriptionRepository;
        this.shareRequestRepository = shareRequestRepository;
        this.notificationService = notificationService;
        this.keywordSimilarityThreshold = keywordSimilarityThreshold;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        Post post = event.getPost();
        List<KeywordSubscription> subscriptions = keywordSubscriptionRepository.findSimilarByPostEmbedding(
                post.getId(),
                post.getGiverId(),
                keywordSimilarityThreshold
        );

        for (KeywordSubscription subscription : subscriptions) {
            String message = "관심 키워드 '" + subscription.getKeyword() + "'와 유사한 새 게시글이 등록되었습니다: "
                    + post.getTitle();
            notificationService.createNotification(
                    subscription.getUserId(),
                    NotificationType.KEYWORD_MATCH,
                    message,
                    post.getId()
            );
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRequestCreated(RequestCreatedEvent event) {
        notificationService.createNotification(
                event.getGiverId(),
                NotificationType.REQUEST_RECEIVED,
                "새로운 나눔 요청이 도착했습니다",
                event.getPostId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRequestApproved(RequestApprovedEvent event) {
        ShareRequest request = event.getRequest();
        notificationService.createNotification(
                event.getReceiverId(),
                NotificationType.REQUEST_APPROVED,
                "나눔 요청이 승인되었습니다",
                request.getPostId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRequestRejected(RequestRejectedEvent event) {
        ShareRequest request = event.getRequest();
        notificationService.createNotification(
                event.getReceiverId(),
                NotificationType.REQUEST_REJECTED,
                "나눔 요청이 거절되었습니다",
                request.getPostId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostClosed(PostClosedEvent event) {
        Long postId = event.getPostId();
        List<ShareRequest> appliedRequests =
                shareRequestRepository.findByPostIdAndStatus(postId, RequestStatus.APPLIED);

        for (ShareRequest request : appliedRequests) {
            request.setStatus(RequestStatus.REJECTED);
            shareRequestRepository.save(request);
            notificationService.createNotification(
                    request.getReceiverId(),
                    NotificationType.REQUEST_REJECTED,
                    "게시글이 마감되어 나눔 요청이 거절되었습니다",
                    postId);
        }

        if (!appliedRequests.isEmpty()) {
            log.info("Post {} closed: {} applied requests auto-rejected", postId, appliedRequests.size());
        }
    }
}
