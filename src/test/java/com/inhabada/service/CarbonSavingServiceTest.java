package com.inhabada.service;

import com.inhabada.entity.Category;
import com.inhabada.entity.Post;
import com.inhabada.entity.ShareRequest;
import com.inhabada.entity.SubCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarbonSavingServiceTest {

    private CarbonSavingService carbonSavingService;

    @BeforeEach
    void setUp() {
        carbonSavingService = new CarbonSavingService();
    }

    @Test
    void resolveUnitGramUsesSubCategoryValue() {
        Post post = post(Category.DRINK, SubCategory.WATER);

        long unitGram = carbonSavingService.resolveUnitGram(post);

        assertThat(unitGram).isEqualTo(110);
    }

    @Test
    void resolveUnitGramUsesFixedEtcValue() {
        Post post = post(Category.ETC, null);

        long unitGram = carbonSavingService.resolveUnitGram(post);

        assertThat(unitGram).isEqualTo(1_000);
    }

    @Test
    void calculateSnapshotMultipliesUnitGramByQuantityAndSplitsUpward() {
        Post post = post(Category.DRINK, SubCategory.WATER);
        ShareRequest request = new ShareRequest(1L, 2L, "금요일 오후 7시", 3);

        CarbonSavingService.CarbonSavingSnapshot snapshot = carbonSavingService.calculate(post, request);

        assertThat(snapshot.totalGram()).isEqualTo(330);
        assertThat(snapshot.giverGram()).isEqualTo(165);
        assertThat(snapshot.receiverGram()).isEqualTo(165);
    }

    @Test
    void splitParticipantGramRoundsUpWhenTotalIsOdd() {
        assertThat(carbonSavingService.splitParticipantGram(101)).isEqualTo(51);
        assertThat(carbonSavingService.splitParticipantGram(100)).isEqualTo(50);
    }

    private Post post(Category category, SubCategory subCategory) {
        return new Post(
                1L,
                "나눔",
                "설명",
                category,
                subCategory,
                new String[]{"posts/image.jpg"},
                10,
                "학생회관",
                "오늘 오후"
        );
    }
}
