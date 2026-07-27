CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    verification_request_id UUID NOT NULL REFERENCES verification_requests (id),
    actor_id UUID NOT NULL REFERENCES users (id),
    action VARCHAR(20) NOT NULL,
    reason VARCHAR(200),
    dt_created TIMESTAMP NOT NULL
);
