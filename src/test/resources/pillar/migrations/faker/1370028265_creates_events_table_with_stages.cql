-- description: creates events table with stages
-- authoredAt: 1370023265
-- up:

-- stage: 1
CREATE TABLE events (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)

-- stage: 2
CREATE TABLE events2 (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)


-- down:
-- stage: 1
DROP TABLE events

-- stage: 2
DROP TABLE events2