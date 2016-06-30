-- description: creates views table
-- authoredAt: 1370028263000
-- up:

CREATE TABLE views (
  id uuid PRIMARY KEY,
  url text,
  person_id int,
  viewed_at timestamp
)

-- down:

DROP TABLE views