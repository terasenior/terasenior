# Compatibilidad Fase 2: preparación y despliegue acotado

Actualización 2026-09-13: create-entity y update-user están conectadas al transporte
servidor y desplegadas con autorización en el proyecto de desarrollo. Su SQL se
aplicó y la comprobación transaccional sintética pasó con ROLLBACK. Ver
`docs/admin-phase2-proposal/DEPLOYED.md` para alcance y validaciones actuales.
Las demás cinco entradas siguen bloqueadas. Kotlin no se ha conectado.

## Antecedentes de la preparación (estado previo al despliegue)

Preparación adicional acotada: `admin-bounded.mjs` y
`docs/admin-phase2-proposal/BOUNDED.md` incluyen adaptador inyectable y SQL completo
local para create-entity/update-user sin cambio de correo. No están conectados
a estas entradas Deno; no se ha ejecutado SQL ni cambiado RLS. El estado de las
siete entradas descrito abajo sigue vigente.

Las siete entradas Deno reutilizan los nombres camelCase de AdminContracts.kt.
El handler compartido valida el cuerpo, verifica el Bearer con Auth y lee el
perfil del actor en servidor. Nunca usa roles del JWT ni del cuerpo para otorgar
permisos. ADMIN_CENTRO requiere centro activo y licencia vigente y solamente
puede gestionar TERAPEUTA/AUXILIAR de ese centro. SUPER_ADMIN tiene ámbito global.
Las operaciones administrativas sobre la propia cuenta están bloqueadas.
La gestión de centros queda reservada a SUPER_ADMIN.

## Estado y atomicidad

TODAS las operaciones autorizadas responden HTTP 503, success=false,
code=ATOMIC_BACKEND_REQUIRED. Solo hay peticiones GET. No hay adaptador de escritura
ni un interruptor que permita habilitarlo accidentalmente. Esta preparación no
completa el backend operativo. El cliente conserva UnavailableAdminRemoteDataSource
y admin-change-password permanece intacta.

Se inspeccionaron únicamente catálogos remotos el 2026-09-13, con autorización,
en «terasenior's Project». No se consultaron filas de usuarios ni datos clínicos.
Los hallazgos y el bloqueo confirmado se detallan en `SCHEMA-REVIEW.md`.
Una secuencia Auth Admin + PostgREST no es una transacción; una compensación también
puede fallar. Por ello creación, cambios y eliminación no ejecutan escrituras.
El backend futuro debe comprobar permisos y relaciones nuevamente bajo bloqueo
en la misma transacción que muta los datos, evitando carreras entre lectura y uso.
No debe escribir directamente tablas internas de Auth mediante SQL improvisado.

La baja futura tendrá una única autoridad (Auth) y una política verificada de
perfil y dependencias. Hasta verificar FK, cascadas, referencias clínicas y
retención, se rechaza sin borrar nada. No se elimina primero user_profiles.
La creación debe garantizar cuenta y perfil conjuntamente; el cambio de correo
debe garantizar coherencia entre ambos. Estado/licencia serán operaciones separadas
del update ordinario. Hay que decidir y garantizar en transacción si desactivar
un centro desactiva también sus usuarios. No se asume esa cascada.

## Aprobaciones y trabajo pendiente

Para completar: obtener un esquema/FK/triggers fiable, diseñar un mecanismo atómico
compatible con Auth, implementarlo y probar rollback, concurrencia e idempotencia
en un entorno explícitamente autorizado. No se han preparado ni aplicado migraciones.
Despliegue, configuración de secretos y cualquier cambio de BD/RLS/migración o
prueba sobre datos reales requieren autorización explícita y destino confirmado.
SUPABASE_SERVICE_ROLE_KEY solo se lee del entorno Deno; no crear archivos de claves.
Configurar ADMIN_ALLOWED_ORIGINS y mantener verificación JWT de la plataforma.
Las respuestas no exponen errores upstream, tokens ni contraseñas.

Pruebas offline: node --test supabase/functions/_shared/admin-phase2.test.mjs
supabase/functions/admin-change-password/handler.test.mjs
