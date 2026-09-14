-- Persiste el cierre de sesión y permite distinguir una sesión estándar.
BEGIN;
ALTER TABLE public.therapy_sessions
  ADD COLUMN IF NOT EXISTS is_standardized boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS total_hits integer NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS total_errors integer NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS total_duration integer NOT NULL DEFAULT 0;
COMMIT;
