# Fase 2 acotada

Actualización 2026-09-13: el SQL y las dos entradas servidor se han aplicado y
desplegado con autorización explícita. Consultar [DEPLOYED.md](DEPLOYED.md).
El resto de este documento conserva el diseño y los límites de la preparación;
las afirmaciones de "no ejecutado" describen aquella entrega previa.

Esta entrega desarrolla solo create-entity y update-user sin cambio de correo.
`bounded-phase2.sql` es el script completo independiente para revision. No se
combina con schema.sql/operations.sql, que siguen siendo el diseño exploratorio
anterior de integración Auth. No hay migraciones ni SQL ejecutado, siquiera local.

## Comportamiento concreto

- Crear centro: SUPER_ADMIN activo, name/cif no vacíos, campos de contrato cerrados,
  límite 1024 caracteres por texto y 16 KiB JSON en SQL. ID generado en servidor,
  estado ACTIVE. LicenseExpiresAt admite NULL/omisión o fecha ISO con hora y zona;
  PostgreSQL valida además que la fecha exista. Cif obligatorio es regla de API,
  aunque la columna remota permita NULL. No se inventa una restricción de unicidad.
- Actualizar usuario: actor activo, sin autoedición administrativa; ADMIN_CENTRO
  solo TERAPEUTA/AUXILIAR del mismo centro activo y con licencia vigente. SUPER_ADMIN
  puede gestionar ADMIN_CENTRO/TERAPEUTA/AUXILIAR. Cuentas SUPER_ADMIN y promociones
  a ese rol se rechazan en este alcance. No hay comprobación MFA implementada.
- El correo recibido se compara exactamente con user_profiles.email y Auth.email
  bajo bloqueo. Si coincide, no se escribe email. Si difiere de cualquiera,
  EMAIL_CHANGE_UNAVAILABLE antes de cualquier escritura, incluso auditoría/recibo.
  Si se omite, no se modifica ni sincroniza email. No hay triggers nuevos de Auth.
- fullName, phone, centerName, roleId e isActive se aplican en una transacción.
  null/omisión significa KEEP; clearPhone/clearCenterName borran explícitamente.
  entityId SET al centro actual es no-op; cambios de asociación se rechazan por
  ENTITY_TRANSFER_UNAVAILABLE o INVALID_ASSOCIATION. No se alteran relaciones
  clínicas ni se presenta esta restricción como soporte completo de traslados.
- Alta de usuarios, bajas físicas, estado y licencia de centro siguen sin adapter
  operativo en este alcance. No se sustituyen silenciosamente por otras acciones.

## Autorización y permisos

Script de creación única dentro de BEGIN/COMMIT, ejecutable por postgres solo tras
aprobación. Crea admin_phase2_private, receipts, audit, validate_request y run,
y tres wrappers públicos: admin_phase2_execute/result/close.

Se revoca acceso público/anon/authenticated y DML directo de service_role a las
tablas nuevas. service_role recibe USAGE del esquema y EXECUTE de run/wrappers;
puede invocar run directamente, con las mismas validaciones. Los wrappers son
SECURITY INVOKER; run es SECURITY DEFINER propiedad de postgres, search_path vacío,
sin SQL dinámico. validate_request no tiene EXECUTE para el rol servidor.

El propietario postgres tiene permisos amplios: es una limitación deliberada del
borrador, no un supuesto de mínimo privilegio. La alternativa de un owner dedicado
requiere diseñar sus permisos y acceso a datos protegidos sin relajar RLS; no se
simula que esos permisos ya existan. Revisar este punto antes del entorno aislado.
La seguridad depende del canal servidor que obtiene actor_id de Auth, de mantener
el esquema fuera de los expuestos y de verificar los grants efectivos. Ningún
secreto se almacena en archivos ni se añade al cliente. No hay ALTER POLICY,
ENABLE/DISABLE RLS ni cambios en permisos de tablas de aplicación en este script.

## Transacción, bloqueos y auditoría

Orden del camino acotado: advisory lock por actor+request ID, Auth destino FOR
SHARE, perfiles actor/destino ordenados por UUID FOR UPDATE, centros ordenados
FOR SHARE. Los perfiles ya bloqueados estabilizan sus asociaciones; las filas de
centros bloqueadas estabilizan estado/licencia. La vigencia se evalúa con reloj
actual después de esperar. Colisiones del hash solo provocan espera adicional.

Las rutas externas a estas funciones, como baja Auth, pueden adquirir bloqueos
distintos: un deadlock aborta, no prueba que el cliente conozca el resultado.
lock_timeout=5s limita esperas; no se reintenta automáticamente. Se debe configurar
un statement timeout en el entorno aislado/servidor y probar carga/concurrencia.

Mutación + recibo final + auditoría se confirman juntos. Las violaciones de
integridad o rechazos de negocio dentro del bloque de mutación revierten ese
bloque y guardan un resultado fallido saneado. Si falla guardar recibo/auditoría,
se revierte también la mutación exitosa. Errores inesperados/timeouts/deadlocks
se propagan. Validación/autorización previas y correo diferente no escriben nada;
su auditoría de fallo requiere log servidor saneado, sin cuerpo/token/correo.

## Idempotencia y recuperación

El cliente futuro genera Idempotency-Key UUID una sola vez por acción, y conserva
clave + operación + cuerpo hasta resolverla. El adaptador usa X-Admin-Mode:
execute (por defecto), result o close. El cuerpo es el mismo contrato original.
La identidad procede exclusivamente de authenticate(token), nunca del cuerpo.

SQL elimina nulls de KEEP, false de flags de borrado y normaliza UUIDs para comparar
el contrato. No normaliza email. Misma clave con distinto cuerpo/operación produce
IDEMPOTENCY_CONFLICT; una acción corregida necesita clave nueva. No existe TTL ni
limpieza de recibos: eliminarlos permitiría repetir una escritura con una clave
antigua. Aprobar retención antes de producción; contienen datos personales del
patch en esquema privado, aunque audit solo guarda IDs/código/fecha.

Tras timeout o respuesta inválida, OUTCOME_UNKNOWN (202); no afirmar rollback.
result intenta tomar el mismo advisory lock sin esperar: IN_PROGRESS si ocupado;
si está libre devuelve el recibo o NOT_OBSERVED. Este último NO autoriza repetir:
una solicitud original puede no haber llegado aún a PostgreSQL.

Recuperación terminal explícita: close espera el mismo lock. Si existe recibo,
devuelve ese resultado. Si no existe, crea un recibo CANCELLED y auditoría en una
transacción. Toda ejecución tardía encuentra ese recibo y no escribe el negocio.
No deshace operaciones completadas. Un timeout de close sigue siendo incierto:
consultar result y decidir explícitamente otra consulta/cierre, nunca execute.
Solo después de confirmar CANCELLED se puede iniciar una acción con clave nueva.

SQL revalida acceso actual antes de devolver recibos. Si el actor pierde permisos
o el destino deja de ser accesible, devuelve FORBIDDEN y el cliente no puede
resolver el caso por sí mismo. Requiere revisión operativa autorizada por otro
administrador; no se incorpora un endpoint que permita suplantar actor_id.

## Adaptador local y qué está conectado

`supabase/functions/_shared/admin-bounded.mjs` tiene handler completo con CORS,
validación, límite de cuerpo, tiempos máximos, identidad verificada inyectada,
una llamada RPC y filtrado de respuestas. authenticate/rpc son dependencias
obligatorias; sin ellas responde UNAVAILABLE y nunca hace red por defecto.

Los index.ts existentes y el gateway Kotlin NO se han conectado a este adaptador:
siguen bloqueados. El transporte servidor futuro debe implementar authenticate
contra Auth y rpc contra los wrappers con credencial privada del entorno remoto.
Debe conservar SQLSTATE como error.sqlState; no copiar errores upstream al cliente.
Solo errores conocidos P0001 se traducen como rechazo definitivo; otros errores
RPC se tratan conservadoramente como resultado incierto. No se hace fetch real en
este trabajo. No se modificaron admin-change-password ni autoservicio.

## Suspensión de centros adoptada como propuesta

Separar suspensión del centro de is_active individual. Mantener estado propio de
cada usuario al suspender/reactivar; no reactivar usuarios deshabilitados antes.
No se implementa ni se afirma un bloqueo efectivo del acceso: los auxiliares
inspeccionados no verifican centro/licencia. Hace falta revisar RLS, RPC, Storage,
Realtime y sesiones/otros accesos realmente usados antes de habilitarlo. No se
cambia RLS ahora. Se conserva el bloqueo explícito de bajas y cambios de estado.

## Pruebas realizadas y pendientes

Pruebas Node con transporte simulado: validación, autorización JS preexistente,
errores saneados, CORS, identidad, resultado incierto, separación execute/result/
close y ausencia de reintentos. NO prueban PL/pgSQL, roles PostgreSQL ni rollback.
No se ejecuta ningún SQL para validar sintaxis. No se ha compilado Kotlin porque
no se modifica código Kotlin; sus fallos previos no se consideran resueltos.

Antes de probar en entorno aislado autorizado:

1. Confirmar proyecto local/test separado y usar solo actores/datos sintéticos.
2. Verificar versión PostgreSQL, columnas/roles/grants/esquemas expuestos; script
   crea objetos una sola vez y falla ante colisiones. No ejecutar schema.sql viejo.
3. Ejecutar/revisar sintaxis PL/pgSQL y permisos, incluyendo anon/authenticated y
   DML directo denegado; comprobar propietario postgres y revisar alternativa.
4. Probar igualdad/diferencia email (Auth y perfil), patch mixto sin escritura,
   rol/centro/autorización, destino SUPER_ADMIN, NULL/SET/CLEAR y fechas inválidas.
5. Inyectar fallo de auditoría y restricciones: cero cambios parciales. Confirmar
   un único centro y recibo ante dos ejecuciones de la misma clave; conflicto con
   payload diferente. Probar revocación/estado/licencia concurrentes y deadlocks.
6. Simular commit seguido de pérdida de respuesta; consulta debe recuperarlo.
   Simular execute retrasado hasta después de close: debe quedar cancelado sin
   crear centro. Simular timeout de close y pérdida de permisos del actor.
7. Solo después conectar transporte servidor y handlers de estos dos endpoints;
   luego diseñar persistencia de clave y recuperación en cliente. Ningún deploy
   ni prueba real queda autorizado por estos archivos.
