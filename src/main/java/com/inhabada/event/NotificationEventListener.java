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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final KeywordSubscriptionRepository keywordSubscriptionRepository;
    private final ShareRequestRepository shareRequestRepository;
    private final NotificationService notificationService;

    public NotificationEventListener(KeywordSubscriptionRepository keywordSubscriptionRepository,
                                     ShareRequestRepository shareRequestRepository,
                                     NotificationService notificationService) {
        this.keywordSubscriptionRepository = keywordSubscriptionRepository;
        this.shareRequestRepository = shareRequestRepository;
        this.notificationService = notificationService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        Post post = event.getPost();
        String searchText = (post.getTitle() + " " + post.getDescription()).toLowerCase();

        List<KeywordSubscription> allSubscriptions = keywordSubscriptionRepository.findAll();
        Map<String, List<KeywordSubscription>> keywordGroups = allSubscriptions.stream()
                .collect(Collectors.groupingBy(KeywordSubscription::getKeyword));

        for (Map.Entry<String, List<KeywordSubscription>> entry : keywordGroups.entrySet()) {
            String keyword = entry.getKey();
            if (!searchText.contains(keyword.toLowerCase())) {
                continue;
            }
            String message = "관심 키워드 '" + keyword + "'에 매칭되는 새 게시글이 등록되었습니다: " + post.getTitle();
            for (KeywordSubscription sub : entry.getValue()) {
                // 본인 게시글에는 알림 미전송
                if (!sub.getUserId().equals(post.getGiverId())) {
                    notificationService.createNotification(
                            sub.getUserId(), NotificationType.KEYWORD_MATCH, message, post.getId());
                }
            }
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
        List<ShareRequest> pendingRequests =
                shareRequestRepository.findByPostIdAndStatus(postId, RequestStatus.PENDING);

        for (ShareRequest request : pendingRequests) {
            request.setStatus(RequestStatus.REJECTED);
            shareRequestRepository.save(request);
            notificationService.createNotification(
                    request.getReceiverId(),
                    NotificationType.REQUEST_REJECTED,
                    "게시글이 마감되어 나눔 요청이 거절되었습니다",
                    postId);
        }

        if (!pendingRequests.isEmpty()) {
            log.info("Post {} closed: {} pending requests auto-rejected", postId, pendingRequests.size());
        }
    }
}
