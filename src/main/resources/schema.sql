DROP TABLE IF EXISTS recommendations;
CREATE TABLE recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    created_at TIMESTAMP,
    recommendation_type VARCHAR(50)
);

-- Insert sample data
INSERT INTO recommendations (user_id, event_id, score, created_at, recommendation_type) VALUES
(1, 101, 0.95, '2024-05-10 10:00:00', 'PERSONALIZED'),
(1, 102, 0.85, '2024-05-10 10:01:00', 'PERSONALIZED'),
(1, 103, 0.75, '2024-05-10 10:02:00', 'TRENDING'),
(2, 101, 0.80, '2024-05-10 11:00:00', 'TRENDING'),
(2, 104, 0.90, '2024-05-10 11:01:00', 'PERSONALIZED'),
(3, 105, 0.88, '2024-05-10 12:00:00', 'LOCATION_BASED');
