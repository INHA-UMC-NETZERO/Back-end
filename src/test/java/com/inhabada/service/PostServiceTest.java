package com.inhabada.service;

import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.entity.Post;
import com.inhabada.event.PostCreatedEvent;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageUrlResolver imageUrlResolver;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userRepository, imageUrlResolver, eventPublisher);
    }

    @Test
    void createPost_storesAndReturnsAvailableTimeText() {
        CreatePostRequest request = new CreatePostRequest(
                "간식 나눔",
                "남은 간식 나눔합니다",
                "FOOD",
                List.of("posts/snack.jpg"),
                3,
                "평일 오후 6시 이후 가능"
        );

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(imageUrlResolver.toUrls(any())).thenReturn(List.of("https://example.com/posts/snack.jpg"));

        PostDetailResponse response = postService.createPost(1L, request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAvailableTime()).isEqualTo("평일 오후 6시 이후 가능");
        assertThat(response.availableTime()).isEqualTo("평일 오후 6시 이후 가능");
        verify(eventPublisher).publishEvent(any(PostCreatedEvent.class));
    }
}
