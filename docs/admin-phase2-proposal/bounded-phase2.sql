-- PROPUESTA LOCAL EJECUTABLE. NO EJECUTADA. NO ES UNA MIGRACION APROBADA.
-- Ejecutar solo tras revision en entorno aislado. Requiere rol postgres.
-- Creacion unica: falla si los objetos existen; no reemplaza funciones existentes.
-- No modifica RLS, Auth, triggers existentes ni permisos de tablas de aplicacion.
BEGIN;
SET LOCAL ROLE postgres;
CREATE SCHEMA admin_phase2_private AUTHORIZATION postgres;
REVOKE ALL ON SCHEMA admin_phase2_private FROM PUBLIC, anon, authenticated, service_role;
GRANT USAGE ON SCHEMA admin_phase2_private TO service_role;

CREATE TABLE admin_phase2_private.receipts (
  actor_id uuid NOT NULL,
  request_id uuid NOT NULL,
  operation text NOT NULL CHECK (operation IN ('admin-create-entity','admin-update-user')),
  target_id uuid,
  request jsonb NOT NULL CHECK (jsonb_typeof(request) = 'object'),
  result jsonb NOT NULL CHECK (jsonb_typeof(result) = 'object'),
  finished_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  PRIMARY KEY (actor_id, request_id)
);
CREATE TABLE admin_phase2_private.audit (
  actor_id uuid NOT NULL,
  request_id uuid NOT NULL,
  operation text NOT NULL,
  target_id uuid,
  code text NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  PRIMARY KEY (actor_id, request_id)
);
REVOKE ALL ON ALL TABLES IN SCHEMA admin_phase2_private
  FROM PUBLIC, anon, authenticated, service_role;
-- Esquema privado no expuesto, sin DML cliente/servidor. No se cambia RLS.

CREATE FUNCTION admin_phase2_private.validate_request(op text, body jsonb)
RETURNS jsonb LANGUAGE plpgsql SET search_path = '' AS $$
DECLARE
  allowed text[]; k text; v jsonb; result jsonb := '{}'::jsonb;
  parsed_date timestamptz;
BEGIN
  IF op NOT IN ('admin-create-entity','admin-update-user') OR op IS NULL THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='OPERATION_BLOCKED';
  END IF;
  IF body IS NULL OR jsonb_typeof(body) <> 'object' OR octet_length(body::text)>16384 THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
  END IF;
  allowed := CASE op WHEN 'admin-create-entity' THEN
    ARRAY['name','cif','address','logoUrl','licenseExpiresAt'] ELSE
    ARRAY['targetUserId','email','fullName','phone','roleId','entityId','isActive','centerName','clearPhone','clearCenterName'] END;
  FOR k,v IN SELECT key,value FROM jsonb_each(body) LOOP
    IF NOT (k=ANY(allowed)) THEN
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
    END IF;
    -- Omitidos y null son KEEP; false en flags de borrado equivale a omision.
    IF v='null'::jsonb THEN CONTINUE; END IF;
    IF k IN ('isActive','clearPhone','clearCenterName') THEN
      IF jsonb_typeof(v)<>'boolean' THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END IF;
      IF k<>'isActive' AND v='false'::jsonb THEN CONTINUE; END IF;
    ELSIF k='entityId' THEN
      IF jsonb_typeof(v)<>'object' THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END IF;
      IF EXISTS(SELECT 1 FROM jsonb_object_keys(v) AS keys(key) WHERE key NOT IN ('action','value'))
        OR coalesce(v->>'action','') NOT IN ('SET','CLEAR') THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END IF;
      IF v->>'action'='SET' THEN
        IF jsonb_typeof(v->'value') IS DISTINCT FROM 'string' OR
          (v->>'value') !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
        END IF;
        v := jsonb_build_object('action','SET','value',lower(v->>'value'));
      ELSE
        IF coalesce(v->'value','null'::jsonb)<>'null'::jsonb THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
        END IF;
        v := jsonb_build_object('action','CLEAR');
      END IF;
    ELSE
      IF jsonb_typeof(v)<>'string' OR length(v #>> '{}')>1024 THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END IF;
    END IF;
    result := result || jsonb_build_object(k,v);
  END LOOP;
  IF op='admin-create-entity' THEN
    IF coalesce(result->>'name','') !~ '[^[:space:]]' OR coalesce(result->>'cif','') !~ '[^[:space:]]' THEN
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
    END IF;
    IF result ? 'licenseExpiresAt' THEN
      IF (result->>'licenseExpiresAt') !~ '^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,6})?(Z|[+-]\d{2}:\d{2})$' THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END IF;
      BEGIN
        parsed_date := (result->>'licenseExpiresAt')::timestamptz;
        IF NOT isfinite(parsed_date) THEN RAISE EXCEPTION 'invalid date'; END IF;
      EXCEPTION WHEN OTHERS THEN
        RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
      END;
    END IF;
  ELSE
    IF coalesce(result->>'targetUserId','') !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
      OR (result ? 'fullName' AND (result->>'fullName') !~ '[^[:space:]]')
      OR (result ? 'email' AND (result->>'email') !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$')
      OR (result ? 'roleId' AND result->>'roleId' NOT IN ('SUPER_ADMIN','ADMIN_CENTRO','TERAPEUTA','AUXILIAR'))
      OR (result ? 'clearPhone' AND result ? 'phone')
      OR (result ? 'clearCenterName' AND result ? 'centerName') THEN
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
    END IF;
    result := jsonb_set(result,'{targetUserId}',to_jsonb(lower(result->>'targetUserId')));
    -- Correo: comparacion exacta bajo bloqueo, no normalizacion silenciosa.
  END IF;
  RETURN result;
END;
$$;

CREATE FUNCTION admin_phase2_private.run(
  p_actor_id uuid, p_request_id uuid, p_operation text, p_body jsonb, p_mode text
) RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path = ''
SET lock_timeout = '5s' AS $$
DECLARE
  body jsonb; actor public.user_profiles%ROWTYPE; target public.user_profiles%ROWTYPE;
  center public.entities%ROWTYPE; previous admin_phase2_private.receipts%ROWTYPE;
  auth_email text; destination uuid; destination_exists boolean := false;
  target_id uuid; new_role text; final_result jsonb; error_code text;
  lock_id bigint; row_item record;
BEGIN
  IF p_actor_id IS NULL OR p_request_id IS NULL OR
     p_mode IS NULL OR p_mode NOT IN ('execute','result','close') THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
  END IF;
  body := admin_phase2_private.validate_request(p_operation,p_body);
  target_id := (body->>'targetUserId')::uuid;
  -- Las colisiones del hash solo serializan claves distintas, no mezclan recibos.
  lock_id := hashtextextended(p_actor_id::text || ':' || p_request_id::text, 0);
  IF p_mode='result' THEN
    IF NOT pg_try_advisory_xact_lock(lock_id) THEN
      RETURN jsonb_build_object('success',false,'code','IN_PROGRESS');
    END IF;
  ELSE
    PERFORM pg_advisory_xact_lock(lock_id);
  END IF;
  -- Auth primero: nunca se escribe. Evita comparar un correo que cambia mientras
  -- se ejecuta el patch. Autorizacion del actor/destino se comprueba mas abajo.
  IF target_id IS NOT NULL THEN
    SELECT email INTO auth_email FROM auth.users WHERE id=target_id FOR SHARE;
  END IF;
  -- Orden comun de perfiles: UUID ascendente. Centros despues, tambien ordenados.
  FOR row_item IN SELECT * FROM public.user_profiles
    WHERE id=p_actor_id OR id=target_id ORDER BY id FOR UPDATE LOOP
    IF row_item.id=p_actor_id THEN actor := row_item; END IF;
    IF row_item.id=target_id THEN target := row_item; END IF;
  END LOOP;
  IF actor.id IS NULL OR actor.is_active IS DISTINCT FROM true OR
    coalesce(actor.role_id,'') NOT IN ('SUPER_ADMIN','ADMIN_CENTRO') THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
  END IF;
  IF p_operation='admin-create-entity' AND actor.role_id<>'SUPER_ADMIN' THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
  END IF;
  destination := target.entity_id;
  IF body ? 'entityId' THEN
    destination := CASE WHEN body->'entityId'->>'action'='SET'
      THEN (body->'entityId'->>'value')::uuid ELSE NULL END;
  END IF;
  FOR row_item IN SELECT * FROM public.entities
    WHERE id=actor.entity_id OR id=target.entity_id OR id=destination ORDER BY id FOR SHARE LOOP
    IF row_item.id=actor.entity_id THEN center := row_item; END IF;
    IF row_item.id=destination THEN destination_exists := true; END IF;
  END LOOP;
  IF actor.role_id='ADMIN_CENTRO' AND (center.id IS NULL OR
    center.status IS DISTINCT FROM 'ACTIVE' OR
    (center.license_expires_at IS NOT NULL AND
      (NOT isfinite(center.license_expires_at) OR center.license_expires_at<=clock_timestamp()))) THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
  END IF;
  IF p_operation='admin-update-user' THEN
    IF target.id IS NULL OR target.id=actor.id OR
      coalesce(target.role_id,'') NOT IN ('ADMIN_CENTRO','TERAPEUTA','AUXILIAR') THEN
      -- SUPER_ADMIN destino protegido en este alcance, incluido email/nombre.
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
    END IF;
    IF actor.role_id='ADMIN_CENTRO' AND
      (target.entity_id IS DISTINCT FROM actor.entity_id OR
       target.role_id NOT IN ('TERAPEUTA','AUXILIAR')) THEN
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
    END IF;
  END IF;
  SELECT * INTO previous FROM admin_phase2_private.receipts
    WHERE actor_id=p_actor_id AND request_id=p_request_id;
  IF FOUND THEN
    IF previous.operation<>p_operation OR previous.request<>body THEN
      RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='IDEMPOTENCY_CONFLICT';
    END IF;
    RETURN previous.result;
  END IF;
  IF p_mode='result' THEN
    RETURN jsonb_build_object('success',false,'code','NOT_OBSERVED');
  END IF;
  IF p_mode='execute' AND p_operation='admin-update-user' AND body ? 'email' AND
    ((body->>'email') IS DISTINCT FROM target.email OR
     (body->>'email') IS DISTINCT FROM auth_email) THEN
    -- Rechazo antes de CUALQUIER INSERT/UPDATE, incluidos recibo y auditoria.
    -- El servidor puede registrar solo el codigo, nunca correo/cuerpo/token.
    RETURN jsonb_build_object('success',false,'code','EMAIL_CHANGE_UNAVAILABLE');
  END IF;
  IF p_mode='close' THEN
    final_result := jsonb_build_object('success',false,'code','CANCELLED');
  ELSE
    -- Subtransaccion: error revierte toda mutacion; solo despues se persiste
    -- el fallo saneado junto con auditoria. Fallo de auditoria revierte todo.
    BEGIN
      IF p_operation='admin-update-user' THEN
        new_role := coalesce(body->>'roleId',target.role_id);
        IF new_role='SUPER_ADMIN' THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='PROTECTED_ROLE';
        END IF;
        IF actor.role_id='ADMIN_CENTRO' AND (new_role NOT IN ('TERAPEUTA','AUXILIAR') OR
          destination IS DISTINCT FROM actor.entity_id) THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='FORBIDDEN';
        END IF;
        IF destination IS NULL OR NOT destination_exists OR NOT EXISTS
          (SELECT 1 FROM public.roles WHERE id=new_role) THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_ASSOCIATION';
        END IF;
        -- No mover un profesional dejando relaciones clinicas en otro centro.
        -- Bloqueo conservador hasta disenar traslado de dependencias.
        IF destination IS DISTINCT FROM target.entity_id THEN
          RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='ENTITY_TRANSFER_UNAVAILABLE';
        END IF;
        UPDATE public.user_profiles SET
          full_name=coalesce(body->>'fullName',full_name),
          phone=CASE WHEN body ? 'clearPhone' THEN NULL ELSE coalesce(body->>'phone',phone) END,
          center_name=CASE WHEN body ? 'clearCenterName' THEN NULL ELSE coalesce(body->>'centerName',center_name) END,
          role_id=new_role,
          is_active=coalesce((body->>'isActive')::boolean,is_active)
        WHERE id=target_id;
        -- Ni email ni entity_id se escriben; mismos valores recibidos son no-op.
        final_result := jsonb_build_object('success',true,'code','COMPLETED');
      ELSE
        target_id := gen_random_uuid();
        INSERT INTO public.entities(id,name,cif,address,logo_url,status,license_expires_at,created_at)
        VALUES(target_id,body->>'name',body->>'cif',body->>'address',body->>'logoUrl',
          'ACTIVE',(body->>'licenseExpiresAt')::timestamptz,clock_timestamp());
        final_result := jsonb_build_object('success',true,'code','COMPLETED',
          'data',jsonb_build_object('entityId',target_id));
      END IF;
    EXCEPTION
      WHEN SQLSTATE 'P0001' THEN
        GET STACKED DIAGNOSTICS error_code = MESSAGE_TEXT;
        IF error_code NOT IN ('EMAIL_CHANGE_UNAVAILABLE','PROTECTED_ROLE','FORBIDDEN',
          'INVALID_ASSOCIATION','ENTITY_TRANSFER_UNAVAILABLE') THEN error_code := 'DATABASE_FAILURE'; END IF;
        final_result := jsonb_build_object('success',false,'code',error_code);
      WHEN integrity_constraint_violation THEN
        final_result := jsonb_build_object('success',false,'code','CONSTRAINT_REJECTED');
      -- Deadlock, timeout, cancellation y fallos inesperados se propagan: resultado
      -- incierto para HTTP, a resolver por result/close. Nunca reintento automatico.
    END;
  END IF;
  INSERT INTO admin_phase2_private.receipts(actor_id,request_id,operation,target_id,request,result)
    VALUES(p_actor_id,p_request_id,p_operation,target_id,body,final_result);
  INSERT INTO admin_phase2_private.audit(actor_id,request_id,operation,target_id,code)
    VALUES(p_actor_id,p_request_id,p_operation,target_id,final_result->>'code');
  RETURN final_result;
END;
$$;

CREATE FUNCTION public.admin_phase2_execute(p_actor_id uuid,p_request_id uuid,p_operation text,p_body jsonb)
RETURNS jsonb LANGUAGE sql SECURITY INVOKER SET search_path = '' AS $$
  SELECT admin_phase2_private.run(p_actor_id,p_request_id,p_operation,p_body,'execute');
$$;
CREATE FUNCTION public.admin_phase2_result(p_actor_id uuid,p_request_id uuid,p_operation text,p_body jsonb)
RETURNS jsonb LANGUAGE sql SECURITY INVOKER SET search_path = '' AS $$
  SELECT admin_phase2_private.run(p_actor_id,p_request_id,p_operation,p_body,'result');
$$;
CREATE FUNCTION public.admin_phase2_close(p_actor_id uuid,p_request_id uuid,p_operation text,p_body jsonb)
RETURNS jsonb LANGUAGE sql SECURITY INVOKER SET search_path = '' AS $$
  SELECT admin_phase2_private.run(p_actor_id,p_request_id,p_operation,p_body,'close');
$$;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA admin_phase2_private FROM PUBLIC,anon,authenticated,service_role;
GRANT EXECUTE ON FUNCTION admin_phase2_private.run(uuid,uuid,text,jsonb,text) TO service_role;
REVOKE ALL ON FUNCTION public.admin_phase2_execute(uuid,uuid,text,jsonb),
  public.admin_phase2_result(uuid,uuid,text,jsonb), public.admin_phase2_close(uuid,uuid,text,jsonb)
  FROM PUBLIC,anon,authenticated,service_role;
GRANT EXECUTE ON FUNCTION public.admin_phase2_execute(uuid,uuid,text,jsonb),
  public.admin_phase2_result(uuid,uuid,text,jsonb), public.admin_phase2_close(uuid,uuid,text,jsonb)
  TO service_role;
COMMIT;
