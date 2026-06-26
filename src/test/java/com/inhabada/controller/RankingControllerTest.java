package com.inhabada.controller;

import com.inhabada.dto.CarbonRankingItem;
import com.inhabada.dto.CarbonRankingResponse;
import com.inhabada.service.RankingService;
import com.inhabada.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private SessionService sessionService;

    @Test
    void getMonthlyCarbonRankingReturnsRankingFields() throws Exception {
        when(sessionService.validateSession("token")).thenReturn(1L);
        when(rankingService.getMonthlyCarbonRanking(5)).thenReturn(new CarbonRankingResponse(
                "2026-06",
                List.of(
                        new CarbonRankingItem(1, 10L, "인하대 학생회", 45_200L),
                        new CarbonRankingItem(2, 20L, "공과대학", 32_800L)
                )
        ));

        mockMvc.perform(get("/api/rankings/carbon/monthly")
                        .param("limit", "5")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-06"))
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].userId").value(10))
                .andExpect(jsonPath("$.items[0].nickname").value("인하대 학생회"))
                .andExpect(jsonPath("$.items[0].carbonSavingGram").value(45200))
                .andExpect(jsonPath("$.items[1].rank").value(2))
                .andExpect(jsonPath("$.items[1].carbonSavingGram").value(32800));
    }
}
