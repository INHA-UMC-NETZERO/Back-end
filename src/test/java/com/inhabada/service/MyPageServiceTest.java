package com.inhabada.service;

import com.inhabada.dto.MyPageSummaryResponse;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.ShareRequest;
import com.inhabada.entity.SubCategory;
import com.inhabada.entity.User;
import com.inhabada.repository.MonthlyCarbonProjection;
import com.inhabada.repository.PostRepository;
import com.inhabada.repository.ShareRequestRepository;
import com.inhabada.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ShareRequestRepository shareRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageUrlResolver imageUrlResolver;

    @Mock
    private CarbonSavingService carbonSavingService;

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageService(
                postRepository,
                shareRequestRepository,
                userRepository,
                imageUrlResolver,
                carbonSavingService
        );
    }

    @Test
    void getSummaryReturnsProfileActivityCarbonAndRecentSixMonths() {
        User user = new User("student@inha.edu", "학생회");
        ReflectionTestUtils.setField(user, "id", 1L);
        user.addCarbonSavingGram(6_700L);

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shareRequestRepository.countCompletedGivenByGiverId(1L)).thenReturn(3L);
        when(shareRequestRepository.countCompletedReceivedByReceiverId(1L)).thenReturn(2L);
        when(shareRequestRepository.countAllInvolvingUser(1L)).thenReturn(8L);
        when(shareRequestRepository.countCompletedInvolvingUser(1L)).thenReturn(5L);
        when(shareRequestRepository.sumMonthlyCarbonSavingByUser(any(), any()))
                .thenReturn(List.of(monthly(previousMonth.toString(), 1_200L)));

        MyPageSummaryResponse response = myPageService.getSummary(1L);

        assertThat(response.profile().userId()).isEqualTo(1L);
        assertThat(response.profile().nickname()).isEqualTo("학생회");
        assertThat(response.profile().email()).isEqualTo("student@inha.edu");
        assertThat(response.activity().sharedCount()).isEqualTo(3L);
        assertThat(response.activity().receivedCount()).isEqualTo(2L);
        assertThat(response.activity().completedDeliveryCount()).isEqualTo(5L);
        assertThat(response.activity().deliveryCompletionRate()).isEqualTo(63);
        assertThat(response.carbon().totalCarbonSavingGram()).isEqualTo(6_700L);
        assertThat(response.monthlyCarbon()).hasSize(6);
        assertThat(response.monthlyCarbon().get(4).month()).isEqualTo(previousMonth.toString());
        assertThat(response.monthlyCarbon().get(4).carbonSavingGram()).isEqualTo(1_200L);
        assertThat(response.monthlyCarbon().get(5).month()).isEqualTo(currentMonth.toString());
        assertThat(response.monthlyCarbon().get(5).carbonSavingGram()).isZero();
    }

    @Test
    void getSummaryFillsMissingRecentSixMonthsWithZero() {
        User user = new User("student@inha.edu", "학생회");
        ReflectionTestUtils.setField(user, "id", 1L);

        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shareRequestRepository.countCompletedGivenByGiverId(1L)).thenReturn(0L);
        when(shareRequestRepository.countCompletedReceivedByReceiverId(1L)).thenReturn(0L);
        when(shareRequestRepository.countAllInvolvingUser(1L)).thenReturn(0L);
        when(shareRequestRepository.countCompletedInvolvingUser(1L)).thenReturn(0L);
        when(shareRequestRepository.sumMonthlyCarbonSavingByUser(any(), any()))
                .thenReturn(List.of(monthly(firstMonth.plusMonths(2).toString(), 500L)));

        MyPageSummaryResponse response = myPageService.getSummary(1L);

        assertThat(response.monthlyCarbon()).hasSize(6);
        assertThat(response.monthlyCarbon())
                .extracting("month")
                .containsExactly(
                        firstMonth.toString(),
                        firstMonth.plusMonths(1).toString(),
                        firstMonth.plusMonths(2).toString(),
                        firstMonth.plusMonths(3).toString(),
                        firstMonth.plusMonths(4).toString(),
                        currentMonth.toString()
                );
        assertThat(response.monthlyCarbon())
                .extracting("carbonSavingGram")
                .containsExactly(0L, 0L, 500L, 0L, 0L, 0L);
    }

    @Test
    void getMyPostsIncludesUnitCarbonSavingAndCompletedCarbonSaving() {
        Post post = post(10L, 1L, "물");
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 6, 1, 10, 0));

        ShareRequest completedRequest = new ShareRequest(10L, 2L, "월요일 3시", 2);
        completedRequest.complete(220L, 110L, 110L, LocalDateTime.of(2026, 6, 2, 10, 0));

        ShareRequest appliedRequest = new ShareRequest(10L, 3L, "화요일 4시", 1);

        User receiver = new User("receiver@inha.edu", "수령자");

        when(postRepository.findByGiverIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(post));
        when(shareRequestRepository.findByPostIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(completedRequest, appliedRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        when(imageUrlResolver.firstUrl(new String[]{"water.png"})).thenReturn("https://cdn.example/water.png");
        when(carbonSavingService.resolveUnitGram(post)).thenReturn(110L);

        List<MyPostResponse> responses = myPageService.getMyPosts(1L);

        assertThat(responses).hasSize(1);
        MyPostResponse response = responses.get(0);
        assertThat(response.unitCarbonSavingGram()).isEqualTo(110L);
        assertThat(response.completedCarbonSavingGram()).isEqualTo(110L);
        verify(carbonSavingService).resolveUnitGram(post);
    }

    @Test
    void getMyRequestsIncludesReceiverCarbonSavingAndCompletedAt() {
        Post post = post(10L, 1L, "물");
        ShareRequest request = new ShareRequest(10L, 2L, "월요일 3시", 2);
        LocalDateTime completedAt = LocalDateTime.of(2026, 6, 2, 10, 0);
        request.complete(220L, 110L, 110L, completedAt);

        when(shareRequestRepository.findByReceiverIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(request));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        List<MyRequestResponse> responses = myPageService.getMyRequests(2L);

        assertThat(responses).hasSize(1);
        MyRequestResponse response = responses.get(0);
        assertThat(response.carbonSavingGram()).isEqualTo(110L);
        assertThat(response.completedAt()).isEqualTo(completedAt);
    }

    private MonthlyCarbonProjection monthly(String month, Long carbonSavingGram) {
        return new MonthlyCarbonProjection() {
            @Override
            public String getMonth() {
                return month;
            }

            @Override
            public Long getCarbonSavingGram() {
                return carbonSavingGram;
            }
        };
    }

    private Post post(Long postId, Long giverId, String title) {
        Post post = new Post(
                giverId,
                title,
                "description",
                Category.DRINK,
                SubCategory.WATER,
                new String[]{"water.png"},
                10,
                "정석학술정보관",
                "월요일 3시"
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
