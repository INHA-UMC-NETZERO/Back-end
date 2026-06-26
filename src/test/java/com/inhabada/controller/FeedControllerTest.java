package com.inhabada.controller;

import com.inhabada.dto.PostCard;
import com.inhabada.service.PostService;
import com.inhabada.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedController.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        Page<PostCard> page = new PageImpl<>(List.of());
        when(postService.getActivePosts(any(), any(), any(Pageable.class))).thenReturn(page);
    }

    @Test
    void getPosts_ignoresUnsupportedSortParameterAndUsesServerControlledOrdering() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "created_at,desc"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        verify(postService).getActivePosts(isNull(), isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("created_at")).isNull();
    }

    @Test
    void getPosts_supportsOldestOrderWithWhitelistedSortProperty() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("order", "oldest"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        verify(postService).getActivePosts(isNull(), isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").isAscending()).isTrue();
    }
}
