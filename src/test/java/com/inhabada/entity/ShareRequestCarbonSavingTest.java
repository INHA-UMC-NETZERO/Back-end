package com.inhabada.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShareRequestCarbonSavingTest {

    @Test
    void newRequestHasNoCarbonSavingSnapshot() {
        ShareRequest request = new ShareRequest(1L, 2L, "금요일 오후 7시", 3);

        assertThat(request.getTotalCarbonSavingGram()).isNull();
        assertThat(request.getGiverCarbonSavingGram()).isNull();
        assertThat(request.getReceiverCarbonSavingGram()).isNull();
        assertThat(request.getCompletedAt()).isNull();
    }

    @Test
    void completeStoresCarbonSavingSnapshotAndCompletedAt() {
        ShareRequest request = new ShareRequest(1L, 2L, "금요일 오후 7시", 3);
        LocalDateTime completedAt = LocalDateTime.of(2026, 6, 27, 15, 30);

        request.complete(101L, 51L, 51L, completedAt);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(request.getTotalCarbonSavingGram()).isEqualTo(101L);
        assertThat(request.getGiverCarbonSavingGram()).isEqualTo(51L);
        assertThat(request.getReceiverCarbonSavingGram()).isEqualTo(51L);
        assertThat(request.getCompletedAt()).isEqualTo(completedAt);
    }
}
