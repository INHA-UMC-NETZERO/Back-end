package com.inhabada.service;

import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.SubCategory;
import com.inhabada.event.PostCreatedEvent;
import com.inhabada.exception.ValidationException;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Mock
    private EmbeddingClient embeddingClient;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userRepository, imageUrlResolver, eventPublisher, embeddingClient);
    }

    @Test
    void createPost_storesCategorySubCategoryLocationAndReturnsLabels() {
        CreatePostRequest request = new CreatePostRequest(
                "snack share",
                "sharing leftover snacks",
                Category.FOOD,
                SubCategory.SNACK,
                List.of("posts/snack.jpg"),
                3,
                "building 5 lobby",
                "weekday evening"
        );

        when(embeddingClient.embedPost("snack share", "sharing leftover snacks")).thenReturn("[0.1,0.2,0.3]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 100L);
            return post;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(imageUrlResolver.toUrls(any())).thenReturn(List.of("https://example.com/posts/snack.jpg"));

        PostDetailResponse response = postService.createPost(1L, request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.getCategory()).isEqualTo(Category.FOOD);
        assertThat(saved.getSubCategory()).isEqualTo(SubCategory.SNACK);
        assertThat(saved.getLocation()).isEqualTo("building 5 lobby");
        assertThat(saved.getAvailableTime()).isEqualTo("weekday evening");
        assertThat(response.category()).isEqualTo("FOOD");
        assertThat(response.categoryLabel()).isEqualTo("식품");
        assertThat(response.subCategory()).isEqualTo("SNACK");
        assertThat(response.subCategoryLabel()).isEqualTo("과자");
        assertThat(response.location()).isEqualTo("building 5 lobby");
        verify(embeddingClient).embedPost("snack share", "sharing leftover snacks");
        verify(postRepository).updateEmbedding(100L, "[0.1,0.2,0.3]");
        verify(eventPublisher).publishEvent(any(PostCreatedEvent.class));
    }

    @Test
    void createPost_allowsEtcWithoutSubCategory() {
        CreatePostRequest request = new CreatePostRequest(
                "misc share",
                "sharing misc item",
                Category.ETC,
                null,
                List.of("posts/misc.jpg"),
                1,
                "club room",
                "after 6pm"
        );

        when(embeddingClient.embedPost("misc share", "sharing misc item")).thenReturn("[0.1,0.2,0.3]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 101L);
            return post;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(imageUrlResolver.toUrls(any())).thenReturn(List.of("https://example.com/posts/misc.jpg"));

        PostDetailResponse response = postService.createPost(1L, request);

        assertThat(response.category()).isEqualTo("ETC");
        assertThat(response.categoryLabel()).isEqualTo("기타");
        assertThat(response.subCategory()).isNull();
        assertThat(response.subCategoryLabel()).isNull();
    }

    @Test
    void createPost_failsWhenEmbeddingCreationFails() {
        CreatePostRequest request = new CreatePostRequest(
                "snack share",
                "sharing leftover snacks",
                Category.FOOD,
                SubCategory.SNACK,
                List.of("posts/snack.jpg"),
                3,
                "building 5 lobby",
                "weekday evening"
        );

        when(embeddingClient.embedPost("snack share", "sharing leftover snacks"))
                .thenThrow(new EmbeddingException("embedding failed"));

        assertThatThrownBy(() -> postService.createPost(1L, request))
                .isInstanceOf(EmbeddingException.class);
        verify(postRepository, never()).save(any());
        verify(postRepository, never()).updateEmbedding(any(), any());
    }

    @Test
    void getActivePosts_filtersByCategoryCodeWhenKeywordPresent() {
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.searchByCategoryAndKeyword(PostStatus.ACTIVE, Category.FOOD, "snack", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        postService.getActivePosts("FOOD", " snack ", pageable);

        verify(postRepository).searchByCategoryAndKeyword(PostStatus.ACTIVE, Category.FOOD, "snack", pageable);
        verify(postRepository, never()).searchByKeyword(any(), any(), any());
    }

    @Test
    void createPost_rejectsNonEtcWithoutSubCategory() {
        CreatePostRequest request = new CreatePostRequest(
                "snack share",
                "sharing leftover snacks",
                Category.FOOD,
                null,
                List.of("posts/snack.jpg"),
                3,
                "building 5 lobby",
                "weekday evening"
        );

        assertThatThrownBy(() -> postService.createPost(1L, request))
                .isInstanceOf(ValidationException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_rejectsMismatchedSubCategory() {
        CreatePostRequest request = new CreatePostRequest(
                "snack share",
                "sharing leftover snacks",
                Category.FOOD,
                SubCategory.COFFEE,
                List.of("posts/snack.jpg"),
                3,
                "building 5 lobby",
                "weekday evening"
        );

        assertThatThrownBy(() -> postService.createPost(1L, request))
                .isInstanceOf(ValidationException.class);
        verify(postRepository, never()).save(any());
    }
}
