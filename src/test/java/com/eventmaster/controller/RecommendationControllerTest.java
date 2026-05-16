package com.eventmaster.controller;

import com.eventmaster.model.Recommendation;
import com.eventmaster.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private MockMvc mockMvc;
    private Recommendation rec1;
    private Recommendation rec2;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController).build();

        rec1 = new Recommendation(1L, 10L, 100L, 0.95, "COLLABORATIVE", LocalDateTime.now());
        rec2 = new Recommendation(2L, 10L, 200L, 0.75, "CONTENT_BASED", LocalDateTime.now());
    }

    private RequestPostProcessor auth(String username) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return (MockHttpServletRequest req) -> { req.setUserPrincipal(token); return req; };
    }

    // ─── GET /recommendations ────────────────────────────────────────────────

    @Test
    public void getAllRecommendations_returns200WithList() throws Exception {
        when(recommendationService.getAllRecommendations()).thenReturn(Arrays.asList(rec1, rec2));

        mockMvc.perform(get("/recommendations").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void getAllRecommendations_emptyList_returns200WithEmptyArray() throws Exception {
        when(recommendationService.getAllRecommendations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recommendations").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /recommendations/user/{userId} ──────────────────────────────────

    @Test
    public void getRecommendationsForUser_returns200WithList() throws Exception {
        when(recommendationService.getRecommendationsForUser(10L)).thenReturn(Arrays.asList(rec1, rec2));

        mockMvc.perform(get("/recommendations/user/10").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].eventId").value(100));
    }

    @Test
    public void getRecommendationsForUser_emptyList_returns200WithEmptyArray() throws Exception {
        when(recommendationService.getRecommendationsForUser(99L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recommendations/user/99").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /recommendations/user/{userId}/top ───────────────────────────────

    @Test
    public void getTopRecommendationsForUser_returns200() throws Exception {
        when(recommendationService.getTopRecommendationsForUser(10L)).thenReturn(Arrays.asList(rec1, rec2));

        mockMvc.perform(get("/recommendations/user/10/top").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(0.95));
    }

    // ─── GET /recommendations/user/{userId}/type/{type} ──────────────────────

    @Test
    public void getRecommendationsForUserByType_returns200() throws Exception {
        when(recommendationService.getRecommendationsForUserByType(10L, "COLLABORATIVE"))
                .thenReturn(Collections.singletonList(rec1));

        mockMvc.perform(get("/recommendations/user/10/type/COLLABORATIVE").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recommendationType").value("COLLABORATIVE"));
    }

    // ─── GET /recommendations/user/{userId}/event/{eventId} ──────────────────

    @Test
    public void getRecommendationByUserAndEvent_found_returns200() throws Exception {
        when(recommendationService.getRecommendationByUserAndEvent(10L, 100L)).thenReturn(Optional.of(rec1));

        mockMvc.perform(get("/recommendations/user/10/event/100").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void getRecommendationByUserAndEvent_notFound_returns404() throws Exception {
        when(recommendationService.getRecommendationByUserAndEvent(99L, 99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/recommendations/user/99/event/99").with(auth("alice")))
                .andExpect(status().isNotFound());
    }

    // ─── GET /recommendations/event/{eventId} ────────────────────────────────

    @Test
    public void getRecommendationsForEvent_returns200() throws Exception {
        when(recommendationService.getRecommendationsForEvent(100L)).thenReturn(Collections.singletonList(rec1));

        mockMvc.perform(get("/recommendations/event/100").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── POST /recommendations ────────────────────────────────────────────────

    @Test
    public void createRecommendation_returns200WithSaved() throws Exception {
        when(recommendationService.saveRecommendation(any())).thenReturn(rec1);

        mockMvc.perform(post("/recommendations")
                        .with(auth("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":10,\"eventId\":100,\"score\":0.95,\"recommendationType\":\"COLLABORATIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(0.95));
    }

    // ─── PUT /recommendations/{id} ────────────────────────────────────────────

    @Test
    public void updateRecommendation_returns200WithUpdated() throws Exception {
        Recommendation updated = new Recommendation(1L, 10L, 100L, 0.80, "UPDATED", LocalDateTime.now());
        when(recommendationService.saveRecommendation(any())).thenReturn(updated);

        mockMvc.perform(put("/recommendations/1")
                        .with(auth("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":10,\"eventId\":100,\"score\":0.80,\"recommendationType\":\"UPDATED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0.80))
                .andExpect(jsonPath("$.recommendationType").value("UPDATED"));
    }

    // ─── DELETE /recommendations/{id} ────────────────────────────────────────

    @Test
    public void deleteRecommendation_returns204() throws Exception {
        doNothing().when(recommendationService).deleteRecommendation(1L);

        mockMvc.perform(delete("/recommendations/1").with(auth("alice")))
                .andExpect(status().isNoContent());

        verify(recommendationService).deleteRecommendation(1L);
    }
}
