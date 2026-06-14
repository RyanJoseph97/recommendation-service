package com.eventmaster.service;

import com.eventmaster.client.EventServiceClient;
import com.eventmaster.client.UserServiceClient;
import com.eventmaster.model.CandidateEvent;
import com.eventmaster.model.RecommendedEvent;
import com.eventmaster.model.RsvpSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private EventServiceClient eventServiceClient;

    @InjectMocks
    private RecommendationService recommendationService;

    private static final String USERNAME = "alice";
    private static final String TOKEN = "test-token";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private CandidateEvent candidate(long id, String creator, String category, int going, int interested) {
        CandidateEvent e = new CandidateEvent();
        e.setId(id);
        e.setTitle("Event " + id);
        e.setCreatorUsername(creator);
        e.setCategory(category);
        e.setGoingCount(going);
        e.setInterestedCount(interested);
        return e;
    }

    private RsvpSummary rsvp(long id, String category) {
        RsvpSummary r = new RsvpSummary();
        r.setId(id);
        r.setCategory(category);
        return r;
    }

    @Test
    public void getRecommendations_emptyEverything_returnsEmpty() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Collections.emptyList());

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getRecommendations_socialScore_followedCreatorRanksHigher() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(List.of("bob"));
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent byBob = candidate(1L, "bob", "MUSIC", 0, 0);
        CandidateEvent byCarol = candidate(2L, "carol", "MUSIC", 0, 0);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(byBob, byCarol));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    public void getRecommendations_excludesAlreadyRsvpedEvents() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(List.of(rsvp(1L, "MUSIC")));

        CandidateEvent rsvpedEvent = candidate(1L, "bob", "MUSIC", 5, 5);
        CandidateEvent otherEvent = candidate(2L, "carol", "SPORTS", 0, 0);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(rsvpedEvent, otherEvent));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void getRecommendations_excludesOwnEvents() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent ownEvent = candidate(1L, USERNAME, "MUSIC", 5, 5);
        CandidateEvent otherEvent = candidate(2L, "bob", "SPORTS", 0, 0);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(ownEvent, otherEvent));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void getRecommendations_categoryScore_rsvpHistoryBoostsSameCategory() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(List.of(rsvp(99L, "MUSIC")));

        CandidateEvent musicEvent = candidate(1L, "bob", "MUSIC", 0, 0);
        CandidateEvent sportsEvent = candidate(2L, "carol", "SPORTS", 0, 0);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(musicEvent, sportsEvent));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    public void getRecommendations_popularityScore_higherEngagementRanksHigher() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent popular = candidate(1L, "bob", "OTHER", 50, 30);
        CandidateEvent unpopular = candidate(2L, "carol", "OTHER", 0, 0);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(popular, unpopular));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    public void getRecommendations_respectsLimit() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        List<CandidateEvent> candidates = Arrays.asList(
                candidate(1L, "a", "OTHER", 0, 0),
                candidate(2L, "b", "OTHER", 0, 0),
                candidate(3L, "c", "OTHER", 0, 0));
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(candidates);

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 2);

        assertEquals(2, result.size());
    }

    @Test
    public void getRecommendations_nullEngagementCounts_treatedAsZero() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent e = candidate(1L, "bob", "OTHER", 0, 0);
        e.setGoingCount(null);
        e.setInterestedCount(null);
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(e));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getScore(), 0.001);
    }

    @Test
    public void getRecommendations_urgency_soonerEventRanksHigher() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent soon = candidate(1L, "bob", "OTHER", 0, 0);
        soon.setStartTime(LocalDateTime.now().plusDays(1));

        CandidateEvent distant = candidate(2L, "carol", "OTHER", 0, 0);
        distant.setStartTime(LocalDateTime.now().plusDays(20));

        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(soon, distant));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    public void getRecommendations_popularityDecay_recentEventBeatsOlderEventWithSameEngagement() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        CandidateEvent recent = candidate(1L, "bob", "OTHER", 100, 0);
        recent.setCreatedAt(LocalDateTime.now().minusDays(1));
        recent.setStartTime(LocalDateTime.now().plusDays(7));

        CandidateEvent stale = candidate(2L, "carol", "OTHER", 100, 0);
        stale.setCreatedAt(LocalDateTime.now().minusDays(60));
        stale.setStartTime(LocalDateTime.now().plusDays(7));

        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(stale, recent));

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 20);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    public void getRecommendations_diversityCap_limitsEventsPerCreator() {
        when(userServiceClient.getFollowingUsernames(USERNAME, TOKEN)).thenReturn(Collections.emptyList());
        when(eventServiceClient.getRsvpedEvents(USERNAME, TOKEN)).thenReturn(Collections.emptyList());

        List<CandidateEvent> candidates = Arrays.asList(
                candidate(1L, "prolific", "OTHER", 0, 0),
                candidate(2L, "prolific", "OTHER", 0, 0),
                candidate(3L, "prolific", "OTHER", 0, 0),
                candidate(4L, "other", "OTHER", 0, 0));
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(candidates);

        List<RecommendedEvent> result = recommendationService.getRecommendations(USERNAME, TOKEN, 10);

        long fromProlific = result.stream()
                .filter(r -> "prolific".equals(r.getCreatorUsername()))
                .count();
        assertEquals(2, fromProlific);
        assertEquals(3, result.size());
    }
}
