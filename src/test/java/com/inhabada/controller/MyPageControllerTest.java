package com.inhabada.controller;

import com.inhabada.dto.MonthlyCarbonPoint;
import com.inhabada.dto.MyPageActivitySummary;
import com.inhabada.dto.MyPageCarbonSummary;
import com.inhabada.dto.MyPageProfileSummary;
import com.inhabada.dto.MyPageSummaryResponse;
import com.inhabada.dto.MyPostRequestItem;
import com.inhabada.dto.MyPostResponse;
import com.inhabada.dto.MyRequestResponse;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.RequestStatus;
import com.inhabada.service.MyPageService;
import com.inhabada.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageService myPageService;

    @MockBean
    private SessionService sessionService;

    @Test
    void getSummaryReturnsDashboardFields() throws Exception {
        when(sessionService.validateSession("token")).thenReturn(1L);
        when(myPageService.getSummary(1L)).thenReturn(new MyPageSummaryResponse(
                new MyPageProfileSummary(1L, "홍길동", "student@inha.edu", null, null),
                new MyPageActivitySummary(3L, 2L, 5L, 63),
                new MyPageCarbonSummary(6_700L),
                List.of(
                        new MonthlyCarbonPoint("2026-05", 1_200L),
                        new MonthlyCarbonPoint("2026-06", 0L)
                )
        ));

        mockMvc.perform(get("/api/mypage/summary")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.userId").value(1))
                .andExpect(jsonPath("$.profile.nickname").value("홍길동"))
                .andExpect(jsonPath("$.profile.email").value("student@inha.edu"))
                .andExpect(jsonPath("$.activity.sharedCount").value(3))
                .andExpect(jsonPath("$.activity.receivedCount").value(2))
                .andExpect(jsonPath("$.activity.completedDeliveryCount").value(5))
                .andExpect(jsonPath("$.activity.deliveryCompletionRate").value(63))
                .andExpect(jsonPath("$.carbon.totalCarbonSavingGram").value(6700))
                .andExpect(jsonPath("$.monthlyCarbon[0].month").value("2026-05"))
                .andExpect(jsonPath("$.monthlyCarbon[0].carbonSavingGram").value(1200))
                .andExpect(jsonPath("$.monthlyCarbon[1].carbonSavingGram").value(0));
    }

    @Test
    void getMyPostsReturnsCarbonFields() throws Exception {
        when(sessionService.validateSession("token")).thenReturn(1L);
        when(myPageService.getMyPosts(1L)).thenReturn(List.of(new MyPostResponse(
                10L,
                "생수 나눔",
                "음료",
                "생수",
                "정석학술정보관",
                "https://cdn.example/water.png",
                8,
                10,
                110L,
                220L,
                PostStatus.ACTIVE,
                false,
                LocalDateTime.parse("2026-06-27T10:00:00"),
                List.of(new MyPostRequestItem(
                        100L,
                        2L,
                        "신청자",
                        2,
                        "월요일 3시",
                        RequestStatus.COMPLETED,
                        LocalDateTime.parse("2026-06-27T11:00:00")
                ))
        )));

        mockMvc.perform(get("/api/mypage/posts")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(10))
                .andExpect(jsonPath("$[0].unitCarbonSavingGram").value(110))
                .andExpect(jsonPath("$[0].completedCarbonSavingGram").value(220))
                .andExpect(jsonPath("$[0].requests[0].requestId").value(100))
                .andExpect(jsonPath("$[0].requests[0].status").value("완료"));
    }

    @Test
    void getMyRequestsReturnsCarbonFields() throws Exception {
        when(sessionService.validateSession("token")).thenReturn(1L);
        when(myPageService.getMyRequests(1L)).thenReturn(List.of(new MyRequestResponse(
                100L,
                10L,
                "생수 나눔",
                "음료",
                "생수",
                "정석학술정보관",
                2,
                "월요일 3시",
                110L,
                LocalDateTime.parse("2026-06-27T12:00:00"),
                RequestStatus.COMPLETED,
                LocalDateTime.parse("2026-06-27T11:00:00")
        )));

        mockMvc.perform(get("/api/mypage/requests")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value(100))
                .andExpect(jsonPath("$[0].carbonSavingGram").value(110))
                .andExpect(jsonPath("$[0].completedAt").value("2026-06-27T12:00:00"))
                .andExpect(jsonPath("$[0].status").value("완료"));
    }
}
