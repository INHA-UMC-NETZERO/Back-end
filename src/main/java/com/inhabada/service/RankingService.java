package com.inhabada.service;

import com.inhabada.dto.CarbonRankingItem;
import com.inhabada.dto.CarbonRankingResponse;
import com.inhabada.repository.ShareRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.IntStream;

@Service
public class RankingService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 50;

    private final ShareRequestRepository shareRequestRepository;

    public RankingService(ShareRequestRepository shareRequestRepository) {
        this.shareRequestRepository = shareRequestRepository;
    }

    @Transactional(readOnly = true)
    public CarbonRankingResponse getMonthlyCarbonRanking(int limit) {
        int normalizedLimit = normalizeLimit(limit);
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime from = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        var rankings = shareRequestRepository.findMonthlyCarbonRanking(from, to, normalizedLimit);
        var items = IntStream.range(0, rankings.size())
                .mapToObj(index -> {
                    var ranking = rankings.get(index);
                    long carbonSavingGram = ranking.getCarbonSavingGram() == null ? 0L : ranking.getCarbonSavingGram();
                    return new CarbonRankingItem(
                            index + 1,
                            ranking.getUserId(),
                            ranking.getNickname(),
                            carbonSavingGram
                    );
                })
                .toList();

        return new CarbonRankingResponse(currentMonth.toString(), items);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
