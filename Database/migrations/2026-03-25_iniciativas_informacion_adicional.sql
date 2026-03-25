-- Migration: iniciativas.informacion_adicional (JSON)
-- Date: 2026-03-25
-- Goal:
--   1) Add dynamic JSON column for initiative custom fields.
--   2) Move known legacy columns (if present) into informacion_adicional keys.

SET @has_info_adicional := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'informacion_adicional'
);
SET @sql_info_adicional := IF(
  @has_info_adicional > 0,
  'SELECT 1',
  'ALTER TABLE iniciativas ADD COLUMN informacion_adicional JSON NULL'
);
PREPARE stmt_info_adicional FROM @sql_info_adicional;
EXECUTE stmt_info_adicional;
DEALLOCATE PREPARE stmt_info_adicional;

SET @has_tipo := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'tipo'
);
SET @sql_tipo := IF(
  @has_tipo > 0,
  'UPDATE iniciativas
   SET informacion_adicional = JSON_SET(COALESCE(informacion_adicional, JSON_OBJECT()), ''$.tipo'', tipo)
   WHERE tipo IS NOT NULL AND TRIM(tipo) <> ''''',
  'SELECT 1'
);
PREPARE stmt_tipo FROM @sql_tipo;
EXECUTE stmt_tipo;
DEALLOCATE PREPARE stmt_tipo;

SET @has_expediente := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'expediente'
);
SET @sql_expediente := IF(
  @has_expediente > 0,
  'UPDATE iniciativas
   SET informacion_adicional = JSON_SET(COALESCE(informacion_adicional, JSON_OBJECT()), ''$.expediente'', expediente)
   WHERE expediente IS NOT NULL AND TRIM(expediente) <> ''''',
  'SELECT 1'
);
PREPARE stmt_expediente FROM @sql_expediente;
EXECUTE stmt_expediente;
DEALLOCATE PREPARE stmt_expediente;

SET @has_objetivo := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'objetivo'
);
SET @sql_objetivo := IF(
  @has_objetivo > 0,
  'UPDATE iniciativas
   SET informacion_adicional = JSON_SET(COALESCE(informacion_adicional, JSON_OBJECT()), ''$.objetivo'', objetivo)
   WHERE objetivo IS NOT NULL AND TRIM(objetivo) <> ''''',
  'SELECT 1'
);
PREPARE stmt_objetivo FROM @sql_objetivo;
EXECUTE stmt_objetivo;
DEALLOCATE PREPARE stmt_objetivo;

SET @has_impacto := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'impacto_principal'
);
SET @sql_impacto := IF(
  @has_impacto > 0,
  'UPDATE iniciativas
   SET informacion_adicional = JSON_SET(COALESCE(informacion_adicional, JSON_OBJECT()), ''$.impacto_principal'', impacto_principal)
   WHERE impacto_principal IS NOT NULL AND TRIM(impacto_principal) <> ''''',
  'SELECT 1'
);
PREPARE stmt_impacto FROM @sql_impacto;
EXECUTE stmt_impacto;
DEALLOCATE PREPARE stmt_impacto;

SET @has_usuarios := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iniciativas'
    AND COLUMN_NAME = 'usuarios_afectados'
);
SET @sql_usuarios := IF(
  @has_usuarios > 0,
  'UPDATE iniciativas
   SET informacion_adicional = JSON_SET(COALESCE(informacion_adicional, JSON_OBJECT()), ''$.usuarios_afectados'', usuarios_afectados)
   WHERE usuarios_afectados IS NOT NULL AND TRIM(usuarios_afectados) <> ''''',
  'SELECT 1'
);
PREPARE stmt_usuarios FROM @sql_usuarios;
EXECUTE stmt_usuarios;
DEALLOCATE PREPARE stmt_usuarios;

-- Validation query: should return zero rows.
SELECT id
FROM iniciativas
WHERE informacion_adicional IS NOT NULL
  AND JSON_VALID(informacion_adicional) = 0;
