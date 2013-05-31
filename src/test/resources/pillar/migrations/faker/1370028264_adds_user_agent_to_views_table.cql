-- description: adds user_agent to views table
-- authoredAt: 1370028264
-- up:

ALTER TABLE views
ADD user_agent text

-- down: