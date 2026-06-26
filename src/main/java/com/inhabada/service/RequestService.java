package com.inhabada.service;

import com.inhabada.dto.CreateRequestDto;
import com.inhabada.dto.RequestResponse;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import com.inhabada.event.RequestApprovedEvent;
import com.inhabada.event.RequestCompletedEvent;
import com.inhabada.event.RequestCreatedEvent;
import com.inhabada.event.RequestRejectedEvent;
import com.inhabada.exception.ConflictException;
import com.inhabada.exception.ForbiddenException;
import com.inhabada.exception.NotFoundException;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.ShareRequestRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestService {

    private final PostRepository postRepository;
    private final ShareRequestRepository shareRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RequestService(PostRepository postRepository,
                          ShareRequestRepository shareRequestRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.shareRequestRepository = shareRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RequestResponse createRequest(Long receiverId, Long postId, CreateRequestDto dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        if (post.getStatus() != PostStatus.ACTIVE || post.getRemainingQuantity() == 0) {
            throw new ConflictException("마감된 게시글입니다");
        }

        if (post.getGiverId().equals(receiverId)) {
            throw new ForbiddenException("본인 게시글에는 요청할 수 없습니다");
        }

        if (dto.quantity() > post.getRemainingQuantity()) {
            throw new ConflictException("요청 수량은 잔여 수량(" + post.getRemainingQuantity() + ") 이하여야 합니다");
        }

        if (shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(postId, receiverId, RequestStatus.PENDING)) {
            throw new ConflictException("이미 처리 중인 요청이 있습니다");
        }

        int pendingSum = shareRequestRepository.sumPendingQuantityByPostId(postId);
        if (pendingSum + dto.quantity() > post.getRemainingQuantity()) {
            int available = post.getRemainingQuantity() - pendingSum;
            throw new ConflictException("현재 요청 가능한 수량은 " + Math.max(available, 0) + "개입니다");
        }

        ShareRequest request = new ShareRequest(postId, receiverId, dto.requestedTime(), dto.quantity());
        ShareRequest saved = shareRequestRepository.save(request);

        eventPublisher.publishEvent(new RequestCreatedEvent(saved, postId, post.getGiverId()));

        return RequestResponse.from(saved);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public void approveRequest(Long requestId, Long giverId) {
        ShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("요청을 찾을 수 없습니다"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        if (!post.getGiverId().equals(giverId)) {
            throw new ForbiddenException("권한이 없습니다");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("이미 처리된 요청입니다");
        }

        if (post.getRemainingQuantity() < request.getQuantity()) {
            throw new ConflictException("잔여 수량이 부족합니다");
        }

        post.setRemainingQuantity(post.getRemainingQuantity() - request.getQuantity());
        request.setStatus(RequestStatus.APPROVED);
        post.setStatus(PostStatus.RESERVED);

        postRepository.save(post);
        shareRequestRepository.save(request);

        eventPublisher.publishEvent(new RequestApprovedEvent(request, request.getReceiverId()));
    }

    @Transactional
    public void rejectRequest(Long requestId, Long giverId) {
        ShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("요청을 찾을 수 없습니다"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        if (!post.getGiverId().equals(giverId)) {
            throw new ForbiddenException("권한이 없습니다");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("이미 처리된 요청입니다");
        }

        request.setStatus(RequestStatus.REJECTED);
        shareRequestRepository.save(request);

        eventPublisher.publishEvent(new RequestRejectedEvent(request, request.getReceiverId()));
    }

    @Transactional
    public void completeRequest(Long requestId, Long giverId) {
        ShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("요청을 찾을 수 없습니다"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        if (!post.getGiverId().equals(giverId)) {
            throw new ForbiddenException("권한이 없습니다");
        }

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new ConflictException("승인된 요청만 완료 처리할 수 있습니다");
        }

        request.setStatus(RequestStatus.COMPLETED);
        post.setStatus(PostStatus.CLOSED);

        postRepository.save(post);
        shareRequestRepository.save(request);

        eventPublisher.publishEvent(new RequestCompletedEvent(request, request.getReceiverId()));
    }
}
