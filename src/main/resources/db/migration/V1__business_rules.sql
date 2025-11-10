CREATE TABLE rule_version (
  id              BIGSERIAL PRIMARY KEY,
  decision_key    VARCHAR(255) NOT NULL,
  version         INT NOT NULL,
  sha256          CHAR(64) NOT NULL,
  xml             BYTEA NOT NULL,
  enabled         BOOLEAN NOT NULL DEFAULT TRUE,
  uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(decision_key, version)
);

CREATE INDEX idx_rule_version_key ON rule_version(decision_key);
