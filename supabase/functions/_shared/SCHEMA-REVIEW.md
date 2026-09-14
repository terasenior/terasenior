# Fase 2: inspección de metadatos, 2026-09-13

Proyecto verificado por nombre: «terasenior's Project».
Solo se ejecutaron SELECT sobre catálogos PostgreSQL: columnas, FK, triggers,
definiciones de funciones públicas y políticas de las tablas administrativas.
No se consultaron registros personales, credenciales ni datos clínicos.
No se ejecutaron funciones de aplicación ni cambios remotos.

## Hechos comprobados

- `on_auth_user_created`, habilitado, ejecuta `handle_new_user()` después de
  insertar en `auth.users`. Es el único trigger no interno encontrado en las
  tablas públicas y en `auth.users`.
- Ese trigger crea exclusivamente `public.profiles`, con rol legacy `THERAPIST`
  y centro NULL. No crea `public.user_profiles`. No acepta privilegios desde
  `raw_user_meta_data`; únicamente obtiene de ahí `full_name`.
- `user_profiles.id` y `profiles.id` referencian `auth.users.id` con DELETE
  CASCADE. Borrar primero el perfil no es un procedimiento válido de baja Auth.
- `activity_results`, `patient_audit_log`, `patient_consents`,
  `patient_observations` y `therapy_sessions` referencian profesionales en
  `user_profiles` sin cascada de borrado. Pueden impedir la baja de Auth.
- `appointment_staff` y `patient_assignments` sí eliminan sus relaciones en
  cascada al borrar el perfil. La tabla legacy `therapist_patients` depende de
  `profiles` con cascada.
- `patients` y `appointments` referencian `entities` con DELETE CASCADE.
  Sus dependencias incluyen resultados y sesiones clínicas con más cascadas.
  `user_profiles.entity_id` impide el borrado mientras existan referencias;
  `profiles.entity_id` usa SET NULL. Comprobar solo usuarios no protege los
  datos clínicos de un centro sin usuarios.
- No hay trigger de sincronización de correo Auth/perfiles ni de propagación
  de estado de centro entre los triggers inspeccionados.
- Las funciones públicas inspeccionadas no incluyen un backend administrativo
  transaccional. `record_user_login()` usa `auth.uid()`; también existe la firma
  UUID antigua. No se ejecutó ninguna. No aparece la RPC de caducidad eliminada.
- Las políticas de entities y user_profiles permiten SELECT/UPDATE según rol
  y centro; no se encontraron políticas INSERT/DELETE para esas tablas.
  No se encontraron políticas de `profiles`. Esto por sí solo no demuestra
  permisos efectivos: esta inspección no certifica grants ni acceso efectivo.
- `fn_get_my_role()` y `fn_get_my_entity()` exigen perfil activo; no verifican
  estado/licencia del centro. Las comprobaciones de las Edge Functions son
  más estrictas que esas funciones auxiliares de RLS.
- `entities.status` es texto nullable y `license_expires_at` es timestamptz
  nullable. `role_id` referencia `roles.id`, pero no se consultaron sus filas:
  no se certifica la existencia de los cuatro identificadores de rol.

## Bloqueo y decisión pendiente

La preparación local sigue devolviendo `ATOMIC_BACKEND_REQUIRED` y no escribe.
La inspección no convierte Auth Admin y PostgREST en una transacción conjunta.
Crear una cuenta Auth no genera el perfil que necesita la aplicación; cambiar
el correo por Auth no sincroniza automáticamente los dos perfiles existentes.

Para completar las garantías previstas hace falta aprobar un diseño de base de
datos/atomicidad: aprovisionamiento seguro del perfil, coherencia del correo,
revalidación de autorización durante la mutación y política de retención/baja.
También debe decidirse si desactivar un centro desactiva sus usuarios.
No se han escrito migraciones ni cambiado arquitectura para resolverlo.
No implementar escrituras parciales con compensaciones presentadas como atómicas.

Siguiente paso propuesto: diseñar para revisión, únicamente en archivos locales,
el mecanismo transaccional y sus contratos SQL/servidor. Su aplicación remota
y las pruebas de integración necesitan autorización independiente y un destino
de pruebas confirmado. Mantener bloqueados los borrados mientras no exista una
política aprobada de conservación de datos y dependencias.
