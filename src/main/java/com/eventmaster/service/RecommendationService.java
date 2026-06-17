package com.eventmaster.service;

import com.eventmaster.client.EventServiceClient;
import com.eventmaster.client.UserServiceClient;
import com.eventmaster.model.CandidateEvent;
import com.eventmaster.model.RecommendedEvent;
import com.eventmaster.model.RsvpSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final double MAX_DISTANCE_KM = 200.0;
    // Engagement half-life: events lose half their popularity weight after 14 days
    private static final double POPULARITY_DECAY_LAMBDA = Math.log(2) / 14.0;
    // Urgency window: events starting today score 1.0; events 21+ days out score 0.0
    private static final double URGENCY_WINDOW_DAYS = 21.0;
    // Diversity cap: at most this many events from the same creator in the result
    private static final int MAX_PER_CREATOR = 2;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private EventServiceClient eventServiceClient;

    @Autowired
    @Qualifier("recommendationExecutor")
    private Executor executor;

    public List<RecommendedEvent> getRecommendations(String username, String token, int limit) {
        if (limit <= 0) return Collections.emptyList();

        CompletableFuture<List<String>> followingF = CompletableFuture.supplyAsync(
                () -> userServiceClient.getFollowingUsernames(username, token), executor);
        CompletableFuture<List<RsvpSummary>> rsvpedF = CompletableFuture.supplyAsync(
                () -> eventServiceClient.getRsvpedEvents(username, token), executor);
        CompletableFuture<List<CandidateEvent>> candidatesF = CompletableFuture.supplyAsync(
                () -> eventServiceClient.getUpcomingPublicEvents(LocalDateTime.now()), executor);
        CompletableFuture<double[]> userCoordsF = CompletableFuture.supplyAsync(
                () -> userServiceClient.getUserCoordinates(username, token), executor);

        Set<String> following = new HashSet<>(followingF.join());
        List<RsvpSummary> rsvped = rsvpedF.join();
        List<CandidateEvent> candidates = candidatesF.join();
        double[] userCoords = userCoordsF.join();

        LocalDateTime now = LocalDateTime.now();

        Set<Long> rsvpedIds = rsvped.stream()
                .map(RsvpSummary::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Long> categoryCounts = rsvped.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(RsvpSummary::getCategory, Collectors.counting()));

        List<CandidateEvent> filtered = candidates.stream()
                .filter(e -> !rsvpedIds.contains(e.getId()))
                .filter(e -> !username.equals(e.getCreatorUsername()))
                .collect(Collectors.toList());

        long maxCategoryCount = categoryCounts.values().stream()
                .mapToLong(Long::longValue).max().orElse(1);

        // Pre-compute decayed popularity for each candidate, then normalize against the max
        double[] decayedEngagement = filtered.stream()
                .mapToDouble(e -> decayedPopularity(e, now))
                .toArray();
        double maxDecayed = Arrays.stream(decayedEngagement).max().orElse(1.0);
        if (maxDecayed == 0.0) maxDecayed = 1.0;

        final boolean hasLocation = userCoords != null;
        final double normMax = maxDecayed;

        List<RecommendedEvent> scored = new ArrayList<>(filtered.size());
        for (int i = 0; i < filtered.size(); i++) {
            CandidateEvent e = filtered.get(i);

            double social = following.contains(e.getCreatorUsername()) ? 1.0 : 0.0;
            double category = categoryCounts.getOrDefault(e.getCategory(), 0L) / (double) maxCategoryCount;
            double popularity = decayedEngagement[i] / normMax;
            double urgency = urgencyScore(e, now);

            double score;
            if (hasLocation && e.getLatitude() != null && e.getLongitude() != null) {
                double distKm = haversineKm(userCoords[0], userCoords[1],
                        e.getLatitude(), e.getLongitude());
                double distance = Math.max(0.0, 1.0 - distKm / MAX_DISTANCE_KM);
                score = 0.25 * social + 0.25 * category + 0.20 * distance + 0.15 * popularity + 0.15 * urgency;
            } else {
                score = 0.30 * social + 0.35 * category + 0.15 * popularity + 0.20 * urgency;
            }
            scored.add(new RecommendedEvent(e, score));
        }

        scored.sort(Comparator.comparingDouble(RecommendedEvent::getScore).reversed());

        // Enforce per-creator diversity: admit at most MAX_PER_CREATOR events per organizer
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Map<String, Integer> creatorCount = new HashMap<>();
        List<RecommendedEvent> result = new ArrayList<>(limit);
        for (RecommendedEvent rec : scored) {
            String creator = rec.getCreatorUsername();
            int count = creatorCount.getOrDefault(creator, 0);
            if (count < MAX_PER_CREATOR) {
                result.add(rec);
                creatorCount.put(creator, count + 1);
                if (result.size() == limit) break;
            }
        }
        return result;
    }

    // log(1 + engagement) weighted by exponential decay since creation.
    // Events created recently carry full engagement weight; older events are discounted.
    private double decayedPopularity(CandidateEvent e, LocalDateTime now) {
        double logEngagement = Math.log1p(safe(e.getGoingCount()) + safe(e.getInterestedCount()));
        if (e.getCreatedAt() == null) return logEngagement;
        double daysSinceCreated = ChronoUnit.HOURS.between(e.getCreatedAt(), now) / 24.0;
        return logEngagement * Math.exp(-POPULARITY_DECAY_LAMBDA * Math.max(0.0, daysSinceCreated));
    }

    // Linear urgency: 1.0 for events starting today, 0.0 for events URGENCY_WINDOW_DAYS+ days out.
    private double urgencyScore(CandidateEvent e, LocalDateTime now) {
        if (e.getStartTime() == null) return 0.0;
        double daysUntil = ChronoUnit.HOURS.between(now, e.getStartTime()) / 24.0;
        if (daysUntil < 0) return 0.0;
        return Math.max(0.0, 1.0 - daysUntil / URGENCY_WINDOW_DAYS);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private int safe(Integer n) {
        return n != null ? n : 0;
    }
}
