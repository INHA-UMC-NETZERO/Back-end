package com.inhabada.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postStatus_acceptsLabelAndCodeAndSerializesAsLabel() throws Exception {
        assertThat(objectMapper.readValue("\"나눔중\"", PostStatus.class)).isEqualTo(PostStatus.ACTIVE);
        assertThat(objectMapper.readValue("\"ACTIVE\"", PostStatus.class)).isEqualTo(PostStatus.ACTIVE);
        assertThat(objectMapper.writeValueAsString(PostStatus.PENDING)).isEqualTo("\"예약중\"");
    }

    @Test
    void requestStatus_acceptsLabelAndCodeAndSerializesAsLabel() throws Exception {
        assertThat(objectMapper.readValue("\"신청중\"", RequestStatus.class)).isEqualTo(RequestStatus.APPLIED);
        assertThat(objectMapper.readValue("\"APPLIED\"", RequestStatus.class)).isEqualTo(RequestStatus.APPLIED);
        assertThat(objectMapper.writeValueAsString(RequestStatus.COMPLETED)).isEqualTo("\"완료\"");
    }
}
