package com.inhabada.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.RequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void postResponses_doNotExposeDuplicateCategoryLabelFields() throws Exception {
        PostCard card = new PostCard(
                1L,
                "snack share",
                "https://example.com/snack.jpg",
                3,
                "식품",
                "과자",
                PostStatus.ACTIVE,
                false
        );
        PostDetailResponse detail = new PostDetailResponse(
                1L,
                "snack share",
                "sharing leftover snacks",
                "식품",
                "과자",
                List.of("https://example.com/snack.jpg"),
                3,
                5,
                "building 5 lobby",
                10L,
                "giver",
                PostStatus.ACTIVE,
                false,
                LocalDateTime.parse("2026-06-27T01:00:00"),
                "weekday evening"
        );

        assertNoDuplicateLabelFields(objectMapper.valueToTree(card));
        assertNoDuplicateLabelFields(objectMapper.valueToTree(detail));
    }

    @Test
    void myPageResponses_doNotExposeDuplicateCategoryLabelFields() {
        MyPostResponse post = new MyPostResponse(
                1L,
                "snack share",
                "식품",
                "과자",
                "building 5 lobby",
                "https://example.com/snack.jpg",
                3,
                5,
                250L,
                125L,
                PostStatus.ACTIVE,
                false,
                LocalDateTime.parse("2026-06-27T01:00:00"),
                List.of()
        );
        MyRequestResponse request = new MyRequestResponse(
                1L,
                2L,
                "snack share",
                "식품",
                "과자",
                "building 5 lobby",
                1,
                "weekday evening",
                null,
                null,
                RequestStatus.APPLIED,
                LocalDateTime.parse("2026-06-27T01:00:00")
        );

        assertNoDuplicateLabelFields(objectMapper.valueToTree(post));
        JsonNode requestJson = objectMapper.valueToTree(request);
        assertThat(requestJson.has("postCategoryLabel")).isFalse();
        assertThat(requestJson.has("postSubCategoryLabel")).isFalse();
    }

    private void assertNoDuplicateLabelFields(JsonNode json) {
        assertThat(json.has("categoryLabel")).isFalse();
        assertThat(json.has("subCategoryLabel")).isFalse();
    }
}
