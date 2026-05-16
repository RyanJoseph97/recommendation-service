package com.eventmaster.service;

import com.eventmaster.model.Recommendation;
import com.eventmaster.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Recommendation rec1;
    private Recommendation rec2;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        rec1 = new Recommendation(1L, 10L, 100L, 0.95, "COLLABORATIVE", LocalDateTime.now());
        rec2 = new Recommendation(2L, 10L, 200L, 0.75, "CONTENT_BASED", LocalDateTime.now());
    }

    @Test
    public void getRecommendationsForUser_returnsListForUser() {
        when(recommendationRepository.findByUserId(10L)).thenReturn(Arrays.asList(rec1, rec2));

        List<Recommendation> result = recommendationService.getRecommendationsForUser(10L);

        assertEquals(2, result.size());
        verify(recommendationRepository).findByUserId(10L);
    }

    @Test
    public void getRecommendationsForUser_emptyList_returnsEmpty() {
        when(recommendationRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<Recommendation> result = recommendationService.getRecommendationsForUser(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getTopRecommendationsForUser_orderedByScoreDesc() {
        when(recommendationRepository.findByUserIdOrderByScoreDesc(10L))
                .thenReturn(Arrays.asList(rec1, rec2));

        List<Recommendation> result = recommendationService.getTopRecommendationsForUser(10L);

        assertEquals(2, result.size());
        assertEquals(0.95, result.get(0).getScore());
        verify(recommendationRepository).findByUserIdOrderByScoreDesc(10L);
    }

    @Test
    public void getRecommendationsForUserByType_filtersCorrectly() {
        when(recommendationRepository.findByUserIdAndRecommendationType(10L, "COLLABORATIVE"))
                .thenReturn(Collections.singletonList(rec1));

        List<Recommendation> result = recommendationService.getRecommendationsForUserByType(10L, "COLLABORATIVE");

        assertEquals(1, result.size());
        assertEquals("COLLABORATIVE", result.get(0).getRecommendationType());
    }

    @Test
    public void getRecommendationsForEvent_returnsList() {
        when(recommendationRepository.findByEventId(100L)).thenReturn(Collections.singletonList(rec1));

        List<Recommendation> result = recommendationService.getRecommendationsForEvent(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getEventId());
    }

    @Test
    public void getRecommendationByUserAndEvent_found_returnsOptional() {
        when(recommendationRepository.findByUserIdAndEventId(10L, 100L)).thenReturn(Optional.of(rec1));

        Optional<Recommendation> result = recommendationService.getRecommendationByUserAndEvent(10L, 100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getEventId());
    }

    @Test
    public void getRecommendationByUserAndEvent_notFound_returnsEmpty() {
        when(recommendationRepository.findByUserIdAndEventId(99L, 99L)).thenReturn(Optional.empty());

        Optional<Recommendation> result = recommendationService.getRecommendationByUserAndEvent(99L, 99L);

        assertFalse(result.isPresent());
    }

    @Test
    public void saveRecommendation_delegatesToRepository() {
        when(recommendationRepository.save(rec1)).thenReturn(rec1);

        Recommendation saved = recommendationService.saveRecommendation(rec1);

        assertEquals(rec1.getId(), saved.getId());
        verify(recommendationRepository).save(rec1);
    }

    @Test
    public void getAllRecommendations_returnsList() {
        when(recommendationRepository.findAll()).thenReturn(Arrays.asList(rec1, rec2));

        List<Recommendation> result = recommendationService.getAllRecommendations();

        assertEquals(2, result.size());
    }

    @Test
    public void deleteRecommendation_callsDeleteById() {
        doNothing().when(recommendationRepository).deleteById(1L);

        recommendationService.deleteRecommendation(1L);

        verify(recommendationRepository).deleteById(1L);
    }
}
