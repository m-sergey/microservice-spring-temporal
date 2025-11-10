CREATE TABLE payment_workflow (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(255) NOT NULL,
  payment_system  BIGSERIAL NOT NULL,
  version         INT NOT NULL,
  UNIQUE(code, payment_system, version)
);
