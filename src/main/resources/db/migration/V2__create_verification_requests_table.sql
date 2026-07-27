CREATE TABLE verification_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL,
    reviewer_id UUID REFERENCES users (id),
    rejection_reason VARCHAR(200),
    dt_submitted TIMESTAMP NOT NULL,
    dt_reviewed TIMESTAMP,
    version BIGINT NOT NULL
);

-- user chi duoc phep co toi da 1 request khi status dang pending
CREATE UNIQUE INDEX uq_user_pending
    ON verification_requests (user_id)
    WHERE status = 'PENDING';
