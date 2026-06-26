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
import com.inhabada.exception.ConflictException;
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
    void createRequest_storesAndReturnsRequestedTimeText() {
        Post post = new Post(
                10L,
                "간식 나눔",
                "남은 간식 나눔합니다",
                "FOOD",
                new String[]{"posts/snack.jpg"},
                3,
                "평일 오후 6시 이후 가능"
        );
        CreateRequestDto dto = new CreateRequestDto(1, "금요일 오후 7시에 받고 싶습니다");

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(shareRequestRepository.existsByPostIdAndReceiverIdAndStatus(any(), any(), any())).thenReturn(false);
        when(shareRequestRepository.sumPendingQuantityByPostId(100L)).thenReturn(0);
        when(shareRequestRepository.save(any(ShareRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponse response = requestService.createRequest(20L, 100L, dto);

        ArgumentCaptor<ShareRequest> requestCaptor = ArgumentCaptor.forClass(ShareRequest.class);
        verify(shareRequestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRequestedTime()).isEqualTo("금요일 오후 7시에 받고 싶습니다");
        assertThat(response.requestedTime()).isEqualTo("금요일 오후 7시에 받고 싶습니다");
        verify(eventPublisher).publishEvent(any(RequestCreatedEvent.class));
    }

    @Test
    void approveRequest_marksRequestApprovedAndPostReserved() {
        Post post = new Post(
                10L,
                "snack share",
                "sharing leftover snacks",
                "FOOD",
                new String[]{"posts/snack.jpg"},
                1,
                "weekday evening"
        );
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.approveRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(post.getStatus()).isEqualTo(PostStatus.RESERVED);
        verify(postRepository).save(post);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestApprovedEvent.class));
    }

    @Test
    void createRequest_rejectsReservedPost() {
        Post post = new Post(
                10L,
                "snack share",
                "sharing leftover snacks",
                "FOOD",
                new String[]{"posts/snack.jpg"},
                3,
                "weekday evening"
        );
        post.setStatus(PostStatus.RESERVED);
        CreateRequestDto dto = new CreateRequestDto(1, "friday evening");

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> requestService.createRequest(20L, 100L, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void completeRequest_marksApprovedRequestCompletedAndPostClosed() {
        Post post = new Post(
                10L,
                "snack share",
                "sharing leftover snacks",
                "FOOD",
                new String[]{"posts/snack.jpg"},
                1,
                "weekday evening"
        );
        post.setStatus(PostStatus.RESERVED);
        ShareRequest request = new ShareRequest(100L, 20L, "friday evening", 1);
        request.setStatus(RequestStatus.APPROVED);

        when(shareRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        requestService.completeRequest(200L, 10L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(post.getStatus()).isEqualTo(PostStatus.CLOSED);
        verify(postRepository).save(post);
        verify(shareRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(RequestCompletedEvent.class));
    }
}
