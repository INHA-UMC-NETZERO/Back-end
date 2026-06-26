package com.inhabada.service;

import com.inhabada.dto.MyPostRequestItem;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyPageActivitySummary;
import com.inhabada.dto.MyPageCarbonSummary;
import com.inhabada.dto.MyPageProfileSummary;
import com.inhabada.dto.MyPageSummaryResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.dto.MonthlyCarbonPoint;
import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.ShareRequest;
import com.inhabada.entity.SubCategory;
import com.inhabada.entity.User;
import com.inhabada.exception.NotFoundException;
import com.inhabada.repository.MonthlyCarbonProjection;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.ShareRequestRepository;
import com.inhabada.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MyPageService {

    private final PostRepository postRepository;
    private final ShareRequestRepository shareRequestRepository;
    private final UserRepository userRepository;
    private final ImageUrlResolver imageUrlResolver;
    private final CarbonSavingService carbonSavingService;

    public MyPageService(PostRepository postRepository,
                         ShareRequestRepository shareRequestRepository,
                         UserRepository userRepository,
                         ImageUrlResolver imageUrlResolver,
                         CarbonSavingService carbonSavingService) {
        this.postRepository = postRepository;
        this.shareRequestRepository = shareRequestRepository;
        this.userRepository = userRepository;
        this.imageUrlResolver = imageUrlResolver;
        this.carbonSavingService = carbonSavingService;
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

    @Transactional(readOnly = true)
    public MyPageSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        long sharedCount = shareRequestRepository.countCompletedGivenByGiverId(userId);
        long receivedCount = shareRequestRepository.countCompletedReceivedByReceiverId(userId);
        long totalRequests = shareRequestRepository.countAllInvolvingUser(userId);
        long completedRequests = shareRequestRepository.countCompletedInvolvingUser(userId);

        return new MyPageSummaryResponse(
                new MyPageProfileSummary(
                        user.getId(),
                        user.getNickname(),
                        user.getEmail(),
                        null,
                        null
                ),
                new MyPageActivitySummary(
                        sharedCount,
                        receivedCount,
                        completedRequests,
                        calculateCompletionRate(totalRequests, completedRequests)
                ),
                new MyPageCarbonSummary(user.getTotalCarbonSavingGram()),
                recentSixMonthCarbon(userId)
        );
    }

    private int calculateCompletionRate(long totalRequests, long completedRequests) {
        if (totalRequests == 0) {
            return 0;
        }
        return (int) Math.round(completedRequests * 100.0 / totalRequests);
    }

    private List<MonthlyCarbonPoint> recentSixMonthCarbon(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);
        LocalDate fromDate = firstMonth.atDay(1);

        Map<String, MonthlyCarbonProjection> carbonByMonth = shareRequestRepository
                .sumMonthlyCarbonSavingByUser(userId, fromDate.atStartOfDay()).stream()
                .collect(Collectors.toMap(MonthlyCarbonProjection::getMonth, Function.identity()));

        return IntStream.rangeClosed(0, 5)
                .mapToObj(firstMonth::plusMonths)
                .map(month -> {
                    MonthlyCarbonProjection projection = carbonByMonth.get(month.toString());
                    long carbonSavingGram = projection == null || projection.getCarbonSavingGram() == null
                            ? 0L
                            : projection.getCarbonSavingGram();
                    return new MonthlyCarbonPoint(month.toString(), carbonSavingGram);
                })
                .toList();
    }

    private MyPostResponse toMyPostResponse(Post post) {
        List<ShareRequest> postRequests = shareRequestRepository.findByPostIdOrderByCreatedAtDesc(post.getId());
        List<MyPostRequestItem> requests = postRequests.stream()
                .map(this::toMyPostRequestItem)
                .toList();

        long completedCarbonSavingGram = postRequests.stream()
                .filter(request -> request.getGiverCarbonSavingGram() != null)
                .mapToLong(ShareRequest::getGiverCarbonSavingGram)
                .sum();
        boolean closed = post.getStatus() == PostStatus.CLOSED || post.getRemainingQuantity() == 0;

        return new MyPostResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory().getLabel(),
                subCategoryLabel(post.getSubCategory()),
                post.getLocation(),
                imageUrlResolver.firstUrl(post.getImageKeys()),
                post.getRemainingQuantity(),
                post.getTotalQuantity(),
                carbonSavingService.resolveUnitGram(post),
                completedCarbonSavingGram,
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
                request.getRequestedTime(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private MyRequestResponse toMyRequestResponse(ShareRequest request) {
        Post post = postRepository.findById(request.getPostId()).orElse(null);
        return new MyRequestResponse(
                request.getId(),
                request.getPostId(),
                post == null ? null : post.getTitle(),
                post == null ? null : post.getCategory().getLabel(),
                post == null ? null : subCategoryLabel(post.getSubCategory()),
                post == null ? null : post.getLocation(),
                request.getQuantity(),
                request.getRequestedTime(),
                request.getReceiverCarbonSavingGram(),
                request.getCompletedAt(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private String subCategoryLabel(SubCategory subCategory) {
        return subCategory == null ? null : subCategory.getLabel();
    }
}
