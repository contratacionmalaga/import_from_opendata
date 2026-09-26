-- Fase 1 de la auditoría de mapeo ATOM.
-- MariaDB: añadir el plazo de recepción de solicitudes de participación.
-- Ejecutar una sola vez tras la copia de seguridad y antes de desplegar la versión que incorpora el campo.

ALTER TABLE tendering_process
    ADD COLUMN participation_request_reception_period DATETIME NULL
    AFTER tender_submission_deadline_period;
