package com.inhabada.service;

import com.inhabada.dto.CarbonRankingItem;
import com.inhabada.dto.CarbonRankingResponse;
import com.inhabada.repository.CarbonRankingProjection;
import com.inhabada.repository.ShareRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private ShareRequestRepository shareRequestRepository;

    private RankingService rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new RankingService(shareRequestRepository);
    }

    @Test
    void getMonthlyCarbonRankingAddsRankToRepositoryOrder() {
        when(shareRequestRepository.findMonthlyCarbonRanking(any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of(
                        projection(10L, "인하대 학생회", 45200L),
                        projection(20L, "공과대학", 32800L)
                ));

        CarbonRankingResponse response = rankingService.getMonthlyCarbonRanking(5);

        assertThat(response.yearMonth()).isEqualTo(YearMonth.now().toString());
        assertThat(response.items())
                .extracting(CarbonRankingItem::rank, CarbonRankingItem::userId, CarbonRankingItem::nickname, CarbonRankingItem::carbonSavingGram)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 10L, "인하대 학생회", 45200L),
                        org.assertj.core.groups.Tuple.tuple(2, 20L, "공과대학", 32800L)
                );
    }

    @Test
    void getMonthlyCarbonRankingUsesDefaultLimitWhenLimitIsInvalid() {
        when(shareRequestRepository.findMonthlyCarbonRanking(any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of());

        rankingService.getMonthlyCarbonRanking(0);

        verify(shareRequestRepository)
                .findMonthlyCarbonRanking(any(LocalDateTime.class), any(LocalDateTime.class), eq(5));
    }

    private CarbonRankingProjection projection(Long userId, String nickname, Long carbonSavingGram) {
        return new CarbonRankingProjection() {
            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getNickname() {
                return nickname;
            }

            @Override
            public Long getCarbonSavingGram() {
                return carbonSavingGram;
            }
        };
    }
}
