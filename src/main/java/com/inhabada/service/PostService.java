package com.inhabada.service;

import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostCard;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.SubCategory;
import com.inhabada.event.PostClosedEvent;
import com.inhabada.event.PostCreatedEvent;
import com.inhabada.exception.ForbiddenException;
import com.inhabada.exception.NotFoundException;
import com.inhabada.exception.ValidationException;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageUrlResolver imageUrlResolver;
    private final ApplicationEventPublisher eventPublisher;
    private final EmbeddingClient embeddingClient;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       ImageUrlResolver imageUrlResolver,
                       ApplicationEventPublisher eventPublisher,
                       EmbeddingClient embeddingClient) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageUrlResolver = imageUrlResolver;
        this.eventPublisher = eventPublisher;
        this.embeddingClient = embeddingClient;
    }

    @Transactional
    public PostDetailResponse createPost(Long giverId, CreatePostRequest request) {
        validateCategory(request.category(), request.subCategory());

        Post post = new Post(
                giverId,
                request.title(),
                request.description(),
                request.category(),
                request.subCategory(),
                request.imageKeys().toArray(new String[0]),
                request.totalQuantity(),
                request.location(),
                request.availableTime()
        );

        String embedding = embeddingClient.embedPost(request.title(), request.description());
        Post saved = postRepository.save(post);
        postRepository.updateEmbedding(saved.getId(), embedding);

        eventPublisher.publishEvent(new PostCreatedEvent(saved));

        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostCard> getActivePosts(String category, String keyword, Pageable pageable) {
        Page<Post> posts;
        boolean hasCategory = StringUtils.hasText(category);
        boolean hasKeyword = StringUtils.hasText(keyword);

        if (hasCategory && hasKeyword) {
            posts = postRepository.searchByCategoryAndKeyword(
                    PostStatus.ACTIVE,
                    parseCategory(category),
                    keyword.trim(),
                    pageable
            );
        } else if (hasKeyword) {
            posts = postRepository.searchByKeyword(PostStatus.ACTIVE, keyword.trim(), pageable);
        } else if (hasCategory) {
            posts = postRepository.findByStatusAndCategory(PostStatus.ACTIVE, parseCategory(category), pageable);
        } else {
            posts = postRepository.findByStatus(PostStatus.ACTIVE, pageable);
        }
        return posts.map(this::toPostCard);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));
        return toDetailResponse(post);
    }

    @Transactional
    public void closePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        if (!post.getGiverId().equals(userId)) {
            throw new ForbiddenException("권한이 없습니다");
        }

        if (post.getStatus() == PostStatus.CLOSED) {
            return;
        }

        post.setStatus(PostStatus.CLOSED);
        postRepository.save(post);

        eventPublisher.publishEvent(new PostClosedEvent(post, post.getId()));
    }

    private PostCard toPostCard(Post post) {
        boolean closed = post.getStatus() == PostStatus.CLOSED || post.getRemainingQuantity() == 0;
        return new PostCard(
                post.getId(),
                post.getTitle(),
                imageUrlResolver.firstUrl(post.getImageKeys()),
                post.getRemainingQuantity(),
                post.getCategory().name(),
                post.getCategory().getLabel(),
                subCategoryCode(post.getSubCategory()),
                subCategoryLabel(post.getSubCategory()),
                post.getStatus(),
                closed
        );
    }

    private PostDetailResponse toDetailResponse(Post post) {
        String giverName = userRepository.findById(post.getGiverId())
                .map(u -> u.getNickname())
                .orElse(null);

        boolean closed = post.getStatus() == PostStatus.CLOSED || post.getRemainingQuantity() == 0;

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getCategory().name(),
                post.getCategory().getLabel(),
                subCategoryCode(post.getSubCategory()),
                subCategoryLabel(post.getSubCategory()),
                imageUrlResolver.toUrls(post.getImageKeys()),
                post.getRemainingQuantity(),
                post.getTotalQuantity(),
                post.getLocation(),
                post.getGiverId(),
                giverName,
                post.getStatus(),
                closed,
                post.getCreatedAt(),
                post.getAvailableTime()
        );
    }

    private void validateCategory(Category category, SubCategory subCategory) {
        if (category == null) {
            throw new ValidationException("카테고리는 필수 항목입니다", List.of("category"));
        }

        if (category == Category.ETC) {
            if (subCategory != null) {
                throw new ValidationException("기타 카테고리는 하위 카테고리를 사용할 수 없습니다", List.of("subCategory"));
            }
            return;
        }

        if (subCategory == null) {
            throw new ValidationException("하위 카테고리는 필수 항목입니다", List.of("subCategory"));
        }

        if (!subCategory.belongsTo(category)) {
            throw new ValidationException("카테고리와 하위 카테고리 조합이 올바르지 않습니다", List.of("category", "subCategory"));
        }
    }

    private Category parseCategory(String category) {
        try {
            return Category.valueOf(category.trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("지원하지 않는 카테고리입니다", List.of("category"));
        }
    }

    private String subCategoryCode(SubCategory subCategory) {
        return subCategory == null ? null : subCategory.name();
    }

    private String subCategoryLabel(SubCategory subCategory) {
        return subCategory == null ? null : subCategory.getLabel();
    }
}
