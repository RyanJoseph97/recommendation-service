package com.eventmaster.controller;

import com.eventmaster.model.RecommendedEvent;
import com.eventmaster.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendedEvent>> getRecommendations(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication,
            HttpServletRequest request) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String token = extractToken(request);
        List<RecommendedEvent> recs = recommendationService.getRecommendations(
                authentication.getName(), token, safeLimit);
        return ResponseEntity.ok(recs);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
