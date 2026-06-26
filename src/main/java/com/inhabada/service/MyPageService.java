package com.inhabada.service;

import com.inhabada.dto.MyPostRequestItem;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.ShareRequest;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.ShareRequestRepository;
import com.inhabada.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MyPageService {

    private final PostRepository postRepository;
    private final ShareRequestRepository shareRequestRepository;
    private final UserRepository userRepository;
    private final ImageUrlResolver imageUrlResolver;

    public MyPageService(PostRepository postRepository,
                         ShareRequestRepository shareRequestRepository,
                         UserRepository userRepository,
                         ImageUrlResolver imageUrlResolver) {
        this.postRepository = postRepository;
        this.shareRequestRepository = shareRequestRepository;
        this.userRepository = userRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    @Transactional(readOnly = true)
    public List<MyPostResponse> getMyPosts(Long userId) {
        return postRepository.findByGiverIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toMyPostResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyRequestResponse> getMyRequests(Long userId) {
        return shareRequestRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toMyRequestResponse)
                .toList();
    }

    private MyPostResponse toMyPostResponse(Post post) {
        List<MyPostRequestItem> requests = shareRequestRepository
                .findByPostIdOrderByCreatedAtDesc(post.getId()).stream()
                .map(this::toMyPostRequestItem)
                .toList();

        boolean closed = post.getStatus() == PostStatus.CLOSED || post.getRemainingQuantity() == 0;

        return new MyPostResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                imageUrlResolver.firstUrl(post.getImageKeys()),
                post.getRemainingQuantity(),
                post.getTotalQuantity(),
                post.getStatus(),
                closed,
                post.getCreatedAt(),
                requests
        );
    }

    private MyPostRequestItem toMyPostRequestItem(ShareRequest request) {
        String receiverName = userRepository.findById(request.getReceiverId())
                .map(u -> u.getNickname())
                .orElse(null);
        return new MyPostRequestItem(
                request.getId(),
                request.getReceiverId(),
                receiverName,
                request.getQuantity(),
                request.getSlotId(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private MyRequestResponse toMyRequestResponse(ShareRequest request) {
        String postTitle = postRepository.findById(request.getPostId())
                .map(Post::getTitle)
                .orElse(null);
        return new MyRequestResponse(
                request.getId(),
                request.getPostId(),
                postTitle,
                request.getQuantity(),
                request.getSlotId(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
