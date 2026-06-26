package com.inhabada.service;

import com.inhabada.dto.CreatePostRequest;
import com.inhabada.dto.PostCard;
import com.inhabada.dto.PostDetailResponse;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.event.PostClosedEvent;
import com.inhabada.event.PostCreatedEvent;
import com.inhabada.exception.ForbiddenException;
import com.inhabada.exception.NotFoundException;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageUrlResolver imageUrlResolver;
    private final ApplicationEventPublisher eventPublisher;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       ImageUrlResolver imageUrlResolver,
                       ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageUrlResolver = imageUrlResolver;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PostDetailResponse createPost(Long giverId, CreatePostRequest request) {
        Post post = new Post(
                giverId,
                request.title(),
                request.description(),
                request.category(),
                request.imageKeys().toArray(new String[0]),
                request.totalQuantity(),
                request.availableTime()
        );

        Post saved = postRepository.save(post);

        eventPublisher.publishEvent(new PostCreatedEvent(saved));

        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostCard> getActivePosts(String category, String keyword, Pageable pageable) {
        Page<Post> posts;
        if (StringUtils.hasText(keyword)) {
            posts = postRepository.searchByKeyword(PostStatus.ACTIVE, keyword.trim(), pageable);
        } else if (StringUtils.hasText(category)) {
            posts = postRepository.findByStatusAndCategory(PostStatus.ACTIVE, category, pageable);
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
                post.getCategory(),
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
                post.getCategory(),
                imageUrlResolver.toUrls(post.getImageKeys()),
                post.getRemainingQuantity(),
                post.getTotalQuantity(),
                post.getGiverId(),
                giverName,
                post.getStatus(),
                closed,
                post.getCreatedAt(),
                post.getAvailableTime()
        );
    }
}
