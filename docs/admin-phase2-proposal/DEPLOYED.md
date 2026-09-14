# Despliegue acotado autorizado — 2026-09-13

## Actualización web — 2026-09-14

Corrección posterior: ambas funciones están en versión 3 e incluyen también
https://www.terasenior.es. Este origen servía la aplicación pero era rechazado
por CORS (HTTP 403); tras la corrección, ambos preflight responden HTTP 204 y
Access-Control-Allow-Origin exacto. No se enviaron formularios ni se modificaron
datos. El alta de usuarios continúa no implementada. Los recibos del intento
reportado no pudieron consultarse por rechazo de la revisión automática.

Las dos funciones están en versión 2 con CORS para https://terasenior.es y
https://terasenior.github.io (configurable mediante ADMIN_ALLOWED_ORIGINS).
EdgeAdminRemoteDataSource conecta creación de centros y edición administrativa
de usuarios desde Kotlin. Las otras operaciones siguen no disponibles.
Las solicitudes pendientes conservan clave y cuerpo en almacenamiento local por
actor y operación, sin tokens ni contraseñas. Un reintento consulta el resultado;
si no fue observado, cierra la clave antes de permitir una nueva solicitud.
La fecha de licencia AAAA-MM-DD del formulario se convierte al final del día UTC.
Las referencias siguientes al cliente desconectado describen el despliegue inicial.

Destino confirmado por el usuario: terasenior's Project, desarrollo,
project ref `qwrykyyjxsfbeazroequ`.

Se aplicó mediante MCP la migración `admin_phase2_bounded_operations`, con el
contenido de bounded-phase2.sql revisado. SHA256 del archivo local aplicado:
`31C90999F4D7662DA95DD952EE06B067DA8C5588CBB59580C7A714437FBC4A91`.
No volver a ejecutar ese script: crea objetos nuevos y no es idempotente.
No se aplicaron schema.sql ni operations.sql.

Se desplegaron y verificaron ACTIVE, versión 1, verify_jwt=true:

- admin-create-entity — ID 5c499eed-f113-4acc-b973-637bb3299097.
- admin-update-user — ID b9b7315c-3340-490b-ba99-7e57fc528834.

Ambas entradas usan admin-bounded-transport.mjs: verifican el token con Auth y
llaman una sola RPC transaccional con identidad derivada del token. SQL carga y
bloquea el perfil del actor y aplica autorización. La clave servidor se obtiene
exclusivamente de SUPABASE_SERVICE_ROLE_KEY en Deno. No se copiaron credenciales.
Los wrappers públicos execute/result/close tienen EXECUTE para service_role y
no para anon ni authenticated, comprobado después del despliegue.
El esquema privado y funciones conservan propietario postgres, con search_path
vacío y sin acceso directo cliente. No se modificaron políticas RLS existentes.

## Validación efectuada

- 30 tests Node PASS: 27 anteriores y 3 de transporte nuevo.
- supabase/tests/admin-bounded-smoke.sql ejecutado en el destino autorizado:
  creación, update con correo idéntico, rechazo de correo distinto sin patch
  parcial, idempotencia, consulta de recibo, cierre y rechazo de ejecución tardía,
  denegación a terapeuta y ausencia de EXECUTE cliente. PASS.
- Fixtures exclusivamente sintéticas, sin contraseñas, dentro de BEGIN/ROLLBACK.
  Ninguna cuenta ni centro de prueba persistió; recibos y auditoría siguen a cero.
- Ambas URLs rechazan POST sin token con HTTP 401.
- No se probó el recorrido HTTP completo con sesión autenticada de un usuario.
  La prueba SQL y los tests de transporte cubren capas distintas, no reemplazan
  esa comprobación de extremo a extremo.

## Uso y límites

POST requiere Authorization Bearer de usuario, JSON e Idempotency-Key UUID.
X-Admin-Mode es execute (defecto), result o close. Mantener clave y cuerpo al
consultar un resultado incierto; no generar una operación nueva automáticamente.
Para navegador, configurar ADMIN_ALLOWED_ORIGINS con orígenes concretos: no se
habilitó un comodín ni se supuso la URL de la aplicación. Clientes sin Origin
pueden invocar el endpoint con autenticación válida.

Crear centros requiere SUPER_ADMIN activo. El update mantiene bloqueados correo
distinto, traslado de centro, gestión/promoción de SUPER_ADMIN y autoedición.
LicenseExpiresAt en creación requiere timestamp ISO con hora y zona, o null.
Las otras cinco funciones no se desplegaron ni habilitaron. admin-change-password
se conservó local, sin desplegar ni cambiar. El cliente Kotlin conserva su gateway
no disponible: conectar pantallas y recuperación de claves queda pendiente.

Sin commit, push, borrados, cambios de contraseñas, modificación de datos existentes
ni despliegue de aplicación. Las 37 incidencias de whitespace previas se conservan.
