-- Migration: iniciativas.expedientes JSON
-- Date: 2026-03-26
-- Goal:
--   1) Add expedientes JSON column to iniciativas.
--   2) Ensure existing rows have a valid JSON array by default.

SET @has_expedientes := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'expedientes'
);
SET @sql_expedientes := IF(
  @has_expedientes > 0,
  'SELECT 1',
  'ALTER TABLE iniciativas ADD COLUMN expedientes JSON NULL'
);
PREPARE stmt_expedientes FROM @sql_expedientes;
EXECUTE stmt_expedientes;
DEALLOCATE PREPARE stmt_expedientes;

UPDATE iniciativas
SET expedientes = JSON_ARRAY()
WHERE expedientes IS NULL;

-- Validation query: should return zero rows.
SELECT id
FROM iniciativas
WHERE expedientes IS NOT NULL
  AND JSON_VALID(expedientes) = 0;
