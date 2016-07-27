-- description: creates users and groups tables
-- authoredAt: 1469630066000
-- up:

-- stage: 1
CREATE TABLE groups (
  id uuid,
  name text,
  PRIMARY KEY (id)
)

-- stage: 2
CREATE TABLE users (
  id uuid,
  group_id uuid,
  username text,
  password text,
  PRIMARY KEY (id)
)


-- down:
-- stage: 1
DROP TABLE users

-- stage: 2
DROP TABLE groups