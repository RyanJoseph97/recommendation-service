# recommendation-service

Stateless personalized event recommendation service for EventMaster. Uses a multi-signal heuristic ranker — no ML model, no persistent store.

Port: `8082`. Context path: `/recommendation-service`.

## Endpoint

```
GET /recommendations?limit=N
Authorization: Bearer <token>
```

Returns up to `N` recommended events for the authenticated caller, sorted by descending score. Each result includes all event fields plus a `score` field.

## Scoring Signals

On each request the service fetches in parallel:
- The caller's following list (user-service)
- The caller's RSVP history (event-service)
- All upcoming public events (event-service)
- The caller's coordinates (user-service, if stored)

Each candidate event is scored on five signals:

| Signal | Description |
|--------|-------------|
| **Social** | 1.0 if the event's creator is followed by the caller, else 0.0 |
| **Category affinity** | Fraction of past RSVPs in this category, relative to the most-attended category |
| **Distance** | `1 - (haversine_km / 200)`, clamped to [0, 1]. Used only when both user and event are geocoded. |
| **Popularity** | `log(1 + going + interested)` weighted by exponential decay since creation (half-life: 14 days) |
| **Urgency** | Linear ramp from 1.0 (today) to 0.0 (21+ days out) |

### Weights

| Condition | Social | Category | Distance | Popularity | Urgency |
|-----------|--------|----------|----------|------------|---------|
| Location available | 25% | 25% | 20% | 15% | 15% |
| No location | 30% | 35% | — | 15% | 20% |

### Filtering and Diversity

Events the caller created or already RSVP'd to are excluded. A per-creator diversity cap of **2 events** prevents any single organizer from dominating the results.

## Stateless Design

This service holds no recommendation data. H2 in-memory is used in both local and Docker profiles (no PostgreSQL dependency). All inputs are fetched from user-service and event-service on each request. Cross-service calls degrade gracefully — failures return an empty list rather than a 500.

## Running Locally

```bash
cd recommendation-service
mvn spring-boot:run
```

Available at `http://localhost:8082/recommendation-service`.

## Environment Variables

| Variable | Required | Default | Notes |
|----------|----------|---------|-------|
| `JWT_SECRET` | No | `eventmaster-shared-dev-secret-key-change-in-prod` | Must match all services |
| `USER_SERVICE_BASE_URL` | No | `http://localhost:8080` | Following list and user coordinates |
| `EVENT_SERVICE_BASE_URL` | No | `http://localhost:8081` | Upcoming events and RSVP history |

## Testing

```bash
mvn test
mvn test -Dtest=RecommendationServiceTest
```
