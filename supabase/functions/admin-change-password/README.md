# Cambio administrativo de contraseña

Preparación local únicamente. Esta función no se ha desplegado ni probado contra Supabase.

El cliente envía `targetUserId` y `newPassword` con su JWT. La función verifica
la identidad mediante Auth y consulta perfiles y centro antes de la única escritura:
`PUT /auth/v1/admin/users/{targetUserId}` con exclusivamente `password`.
Devuelve 204 solo si Auth confirma el éxito. No modifica perfiles, esquema o RLS.

## Permisos

- SUPER_ADMIN activo: otras cuentas con perfil existente.
- ADMIN_CENTRO activo: TERAPEUTA o AUXILIAR de su mismo centro ACTIVE, con
  licencia sin caducidad (null) o fecha futura válida.
- Se deniegan cambios propios, roles desconocidos y datos ausentes o incoherentes.
- El rol, centro y estado se leen en servidor; no se aceptan desde el cliente.

## Pruebas exclusivamente locales

Con Node 24 o posterior:

```text
node --test supabase/functions/admin-change-password/handler.test.mjs
```

Las pruebas importan solo handler.mjs, inyectan fetch simulado y usan valores
sintéticos. No ejecutar index.ts localmente ni crear archivos con credenciales.
La función usa APIs nativas de Deno sin SDK ni dependencias nuevas.

## Pendiente antes de solicitar autorización de despliegue

1. Verificar en un entorno autorizado que `user_profiles.id` corresponde al ID
   de Auth y que roles, centros, estados y licencias tienen los valores esperados.
2. Auditar los permisos existentes: ningún usuario no autorizado debe poder
   modificar `role_id`, `entity_id`, `is_active` o la licencia del centro para
   elevar privilegios. Si hace falta cambiar RLS o esquema, detenerse y solicitar
   autorización independiente. Esta revisión no se ha realizado remotamente.
3. Revisar el runtime Deno y la verificación de JWT de entrada. Mantener
   verificación de JWT para llamadas de usuario; la función también valida el
   token con Auth. No se incluye ni modifica configuración de producción.
4. Confirmar secretos solo en el servidor desplegado: SUPABASE_URL y la variable
   de credencial administrativa SUPABASE_SERVICE_ROLE_KEY. Nunca copiarlos al
   cliente, al repositorio o al entorno local de estas pruebas.
5. Definir ADMIN_PASSWORD_ALLOWED_ORIGINS como lista de orígenes web exactos
   separados por comas. Sin configuración, las peticiones con Origin se rechazan.
   Los clientes nativos sin Origin siguen requiriendo JWT y permisos; CORS no
   sustituye autorización.
6. Revisar política de contraseñas de Auth, limitación de intentos y auditoría
   de actor/destinatario/resultado sin registrar cuerpos, contraseñas o JWT.
   La función no añade almacenamiento de auditoría ni limitador persistente.
7. Acordar y verificar el comportamiento de sesiones existentes tras el cambio;
   no se implementa una revocación adicional ni se promete cierre inmediato.
8. Tras autorización separada, probar con cuentas de prueba aisladas: éxito,
   permisos entre centros, cuentas privilegiadas, JWT caducado, fallo de Auth y
   resultado visible en el diálogo. La sesión del administrador debe conservarse.

El cliente no reintenta automáticamente la escritura. Ante timeout, el cambio
puede haberse aplicado aunque la confirmación no llegue; comprobar antes de repetir.
La lectura de permisos y la llamada a Auth no son una transacción: una revocación
concurrente de permisos requiere valoración operativa antes de producción.

No se requiere una migración para esta implementación. El despliegue y cualquier
prueba con cuentas reales necesitan autorización explícita adicional.
