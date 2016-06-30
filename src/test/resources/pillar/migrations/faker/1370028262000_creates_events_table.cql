-- description: creates events table
-- authoredAt: 1370023262000
-- up:

CREATE TABLE events (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)