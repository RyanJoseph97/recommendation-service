package com.eventmaster.client;

import com.eventmaster.model.CandidateEvent;
import com.eventmaster.model.PageContent;
import com.eventmaster.model.RsvpSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class EventServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceClient.class);

    @Value("${event.service.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<CandidateEvent> getUpcomingPublicEvents(LocalDateTime startAfter) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/events")
                .queryParam("visibility", "PUBLIC")
                .queryParam("startAfter", startAfter.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .queryParam("size", 100)
                .build().encode().toUri();
        try {
            ResponseEntity<PageContent<CandidateEvent>> response = restTemplate.exchange(
                    uri, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageContent<CandidateEvent>>() {});
            if (response.getBody() == null) return Collections.emptyList();
            return response.getBody().getContent();
        } catch (RestClientException e) {
            logger.warn("Could not fetch upcoming public events: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<RsvpSummary> getRsvpedEvents(String username, String token) {
        String url = baseUrl + "/users/" + username + "/rsvped-events";
        try {
            ResponseEntity<List<RsvpSummary>> response = restTemplate.exchange(
                    url, HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<List<RsvpSummary>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            logger.warn("Could not fetch RSVPs for '{}': {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpEntity<?> bearerEntity(String token) {
        if (token == null) return HttpEntity.EMPTY;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
