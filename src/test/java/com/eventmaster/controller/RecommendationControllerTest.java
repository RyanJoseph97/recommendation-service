package com.eventmaster.controller;

import com.eventmaster.model.CandidateEvent;
import com.eventmaster.model.RecommendedEvent;
import com.eventmaster.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController).build();
    }

    private RequestPostProcessor auth(String username) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return (MockHttpServletRequest req) -> { req.setUserPrincipal(token); return req; };
    }

    private RecommendedEvent rec(long id, double score) {
        CandidateEvent e = new CandidateEvent();
        e.setId(id);
        e.setTitle("Event " + id);
        e.setCreatorUsername("bob");
        return new RecommendedEvent(e, score);
    }

    @Test
    public void getRecommendations_returns200WithList() throws Exception {
        List<RecommendedEvent> recs = Arrays.asList(rec(1L, 0.9), rec(2L, 0.7));
        when(recommendationService.getRecommendations(eq("alice"), any(), eq(20))).thenReturn(recs);

        mockMvc.perform(get("/recommendations").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].score").value(0.9))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void getRecommendations_emptyList_returns200WithEmptyArray() throws Exception {
        when(recommendationService.getRecommendations(eq("alice"), any(), eq(20)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recommendations").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getRecommendations_customLimit_passesLimitToService() throws Exception {
        when(recommendationService.getRecommendations(eq("alice"), any(), eq(5)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recommendations").param("limit", "5").with(auth("alice")))
                .andExpect(status().isOk());

        verify(recommendationService).getRecommendations(eq("alice"), any(), eq(5));
    }

    @Test
    public void getRecommendations_passesUsernameFromPrincipal() throws Exception {
        when(recommendationService.getRecommendations(eq("carol"), any(), eq(20)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recommendations").with(auth("carol")))
                .andExpect(status().isOk());

        verify(recommendationService).getRecommendations(eq("carol"), any(), eq(20));
    }
}
