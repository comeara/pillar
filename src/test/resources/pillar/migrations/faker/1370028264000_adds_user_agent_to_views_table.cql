-- description: adds user_agent to views table
-- authoredAt: 1370028264000
-- up:

ALTER TABLE views
ADD user_agent text

-- down: