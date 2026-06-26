package com.inhabada.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyPageSummaryResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void summaryResponseSerializesDashboardShape() throws Exception {
        MyPageSummaryResponse response = new MyPageSummaryResponse(
                new MyPageProfileSummary(1L, "닉네임", "student@inha.edu", "치치", null),
                new MyPageActivitySummary(3L, 2L, 5L, 62),
                new MyPageCarbonSummary(6_700L),
                List.of(
                        new MonthlyCarbonPoint("2026-05", 0L),
                        new MonthlyCarbonPoint("2026-06", 6_700L)
                )
        );

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(root.get("profile").get("nickname").asText()).isEqualTo("닉네임");
        assertThat(root.get("activity").get("sharedCount").asLong()).isEqualTo(3L);
        assertThat(root.get("activity").get("deliveryCompletionRate").asInt()).isEqualTo(62);
        assertThat(root.get("carbon").get("totalCarbonSavingGram").asLong()).isEqualTo(6_700L);
        assertThat(root.get("monthlyCarbon")).hasSize(2);
        assertThat(root.get("monthlyCarbon").get(1).get("month").asText()).isEqualTo("2026-06");
    }
}
