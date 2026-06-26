package com.inhabada.service;

import com.inhabada.dto.CreateRequestDto;
import com.inhabada.dto.RequestResponse;
import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import com.inhabada.entity.SubCategory;
import com.inhabada.event.RequestApprovedEvent;
import com.inhabada.event.RequestCompletedEvent;
import com.inhabada.event.RequestCreatedEvent;
import com.inhabada.event.RequestRejectedEvent;
import com.inhabada.exception.ConflictException;
import com.inhabada.exception.ForbiddenException;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.ShareRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ShareRequestRepository shareRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RequestService requestService;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(postRepository, shareRequestRepository, eventPublisher);
    }

    @Test
    void createRequest_storesRequestAsAppliedAndReturnsRequestedTimeText() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(any(), any(), any())).thenReturn(false);
        when(shareRequestRepository.sumAppliedQuantityByPostId(100L)).thenReturn(0);
        when(shareRequestRepository.save(any(ShareRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponse response = requestService.createRequest(20L, 100L, dto);

        ArgumentCaptor<ShareRequest> requestCaptor = ArgumentCaptor.forClass(ShareRequest.class);
        verify(shareRequestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(RequestStatus.APPLIED);
        assertThat(requestCaptor.getValue().getRequestedTime()).isEqualTo("friday evening");
        assertThat(response.requestedTime()).isEqualTo("friday evening");
        verify(eventPublisher).publishEvent(any(RequestCreatedEvent.class));
    }

    @Test
    void createRequest_locksPostBeforeCheckingAppliedQuantity() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(any(), any(), any())).thenReturn(false);
        when(shareRequestRepository.sumAppliedQuantityByPostId(100L)).thenReturn(0);
        when(shareRequestRepository.save(any(ShareRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestService.createRequest(20L, 100L, dto);

        verify(postRepository).findByIdForUpdate(100L);
    }

    @Test
    void createRequest_rejectsWhenAppliedQuantityWouldExceedRemainingQuantity() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(2, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(any(), any(), any())).thenReturn(false);
        when(shareRequestRepository.sumAppliedQuantityByPostId(100L)).thenReturn(2);

        assertThatThrownBy(() -> requestService.createRequest(20L, 100L, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRequest_rejectsWhenRequestedQuantityExceedsRemainingQuantity() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(4, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.createRequest(20L, 100L, dto))
                .isInstanceOf(ConflictException.class);

        verify(shareRequestRepository, never()).save(any(ShareRequest.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createRequest_rejectsDuplicateAppliedRequestFromSameReceiver() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(100L, 20L, RequestStatus.APPLIED))
                .thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(20L, 100L, dto))
                .isInstanceOf(ConflictException.class);

        verify(shareRequestRepository, never()).sumAppliedQuantityByPostId(any());
        verify(shareRequestRepository, never()).save(any(ShareRequest.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createRequest_rejectsOwnPost() {
        Post post = post();
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.createRequest(10L, 100L, dto))
                .isInstanceOf(ForbiddenException.class);

        verify(shareRequestRepository, never()).save(any(ShareRequest.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void approveRequest_keepsPostActiveWhenRemainingQuantityExists() {
        Post post = post();
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.approveRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(post.getRemainingQuantity()).isEqualTo(2);
        verify(postRepository).save(post);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestApprovedEvent.class));
    }

    @Test
    void approveRequest_marksPostPendingWhenRemainingQuantityBecomesZero() {
        Post post = post();
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 3);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.approveRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(post.getStatus()).isEqualTo(PostStatus.PENDING);
        assertThat(post.getRemainingQuantity()).isZero();
        verify(postRepository).save(post);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestApprovedEvent.class));
    }

    @Test
    void approveRequest_rejectsAlreadyProcessedRequest() {
        Post post = post();
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);
        request.setStatus(RequestStatus.PENDING);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.approveRequest(200L, 10L))
                .isInstanceOf(ConflictException.class);

        verify(postRepository, never()).save(any(Post.class));
        verify(shareRequestRepository, never()).save(any(ShareRequest.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void rejectRequest_marksAppliedRequestRejected() {
        Post post = post();
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.rejectRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestRejectedEvent.class));
    }

    @Test
    void createRequest_rejectsClosedPost() {
        Post post = post();
        post.setStatus(PostStatus.CLOSED);
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.createRequest(20L, 100L, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRequest_allowsPendingPostWhenRemainingQuantityExists() {
        Post post = post();
        post.setStatus(PostStatus.PENDING);
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(any(), any(), any())).thenReturn(false);
        when(shareRequestRepository.sumAppliedQuantityByPostId(100L)).thenReturn(0);
        when(shareRequestRepository.save(any(ShareRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponse response = requestService.createRequest(20L, 100L, dto);

        assertThat(response.status()).isEqualTo(RequestStatus.APPLIED);
        verify(shareRequestRepository).save(any(ShareRequest.class));
        verify(eventPublisher).publishEvent(any(RequestCreatedEvent.class));
    }

    @Test
    void completeRequest_marksPendingRequestCompletedAndPostClosed() {
        Post post = post();
        post.setStatus(PostStatus.PENDING);
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);
        request.setStatus(RequestStatus.PENDING);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.completeRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(post.getStatus()).isEqualTo(PostStatus.CLOSED);
        verify(postRepository).save(post);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestCompletedEvent.class));
    }

    @Test
    void completeRequest_rejectsAppliedRequest() {
        Post post = post();
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.completeRequest(200L, 10L))
                .isInstanceOf(ConflictException.class);

        verify(postRepository, never()).save(any(Post.class));
        verify(shareRequestRepository, never()).save(any(ShareRequest.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    private Post post() {
        return new Post(
                10L,
                "snack share",
                "sharing leftover snacks",
                Category.FOOD,
                SubCategory.SNACK,
                new String[]{"posts/snack.jpg"},
                3,
                "building 5 lobby",
                "weekday evening"
        );
    }
}
