package com.inhabada.service;

import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.ShareRequest;
import org.springframework.stereotype.Service;

@Service
public class CarbonSavingService {

    public static final long ETC_UNIT_CARBON_SAVING_GRAM = 1_000L;

    public long resolveUnitGram(Post post) {
        if (post.getCategory() == Category.ETC) {
            return ETC_UNIT_CARBON_SAVING_GRAM;
        }
        return post.getSubCategory().getUnitCarbonSavingGram();
    }

    public CarbonSavingSnapshot calculate(Post post, ShareRequest request) {
        long totalGram = resolveUnitGram(post) * request.getQuantity();
        long participantGram = splitParticipantGram(totalGram);
        return new CarbonSavingSnapshot(totalGram, participantGram, participantGram);
    }

    public long splitParticipantGram(long totalGram) {
        return (totalGram + 1) / 2;
    }

    public record CarbonSavingSnapshot(
            long totalGram,
            long giverGram,
            long receiverGram
    ) {
    }
}
