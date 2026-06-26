package com.inhabada.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inhabada.dto.LoginRequest;
import com.inhabada.dto.LoginResponse;
import com.inhabada.exception.ValidationException;
import com.inhabada.service.AuthService;
import com.inhabada.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionService sessionService;

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("student@inha.edu", "학생");
        LoginResponse response = new LoginResponse("uuid-token", 1L, "student@inha.edu", "학생");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("uuid-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("student@inha.edu"))
                .andExpect(jsonPath("$.nickname").value("학생"));
    }

    @Test
    void login_shouldReturn400ForInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest("user@gmail.com", "학생");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ValidationException("@inha.edu 이메일만 사용할 수 있습니다"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("@inha.edu 이메일만 사용할 수 있습니다"));
    }

    @Test
    void login_shouldReturn400ForMissingEmail() throws Exception {
        String body = "{\"nickname\": \"학생\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void login_shouldReturn400ForMissingNickname() throws Exception {
        String body = "{\"email\": \"student@inha.edu\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void logout_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isNoContent());

        verify(authService).logout("some-token");
    }

    @Test
    void logout_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
