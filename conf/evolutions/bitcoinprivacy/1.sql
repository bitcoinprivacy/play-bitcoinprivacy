
# --- !Ups

CREATE TABLE access (
  ip        VARCHAR(255) NOT NULL,
  path      VARCHAR(255) NOT NULL,
  created   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE access;
