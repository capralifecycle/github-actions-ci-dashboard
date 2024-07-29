-- See naming schema of these files at https://documentation.red-gate.com/flyway/flyway-cli-and-api/concepts/migrations
CREATE TABLE dashboard_config
(
  id   TEXT PRIMARY KEY,
  data JSONB
);
