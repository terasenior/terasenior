-- Server-only intent and Auth INSERT trigger. Passwords never enter these RPCs.
BEGIN;
CREATE TABLE admin_phase2_private.user_creations (
 actor_id uuid NOT NULL, request_id uuid NOT NULL, ticket uuid NOT NULL UNIQUE DEFAULT gen_random_uuid(),
 body jsonb NOT NULL, state text NOT NULL CHECK(state IN ('PENDING','COMPLETED','CANCELLED')),
 user_id uuid, expires_at timestamptz NOT NULL DEFAULT clock_timestamp()+interval '2 minutes',
 PRIMARY KEY(actor_id,request_id)
);
ALTER TABLE admin_phase2_private.user_creations ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON admin_phase2_private.user_creations FROM PUBLIC,anon,authenticated,service_role;

CREATE FUNCTION admin_phase2_private.authorize_creation(actor_id uuid, body jsonb)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path='' AS $$
DECLARE actor public.user_profiles%ROWTYPE; center public.entities%ROWTYPE; destination uuid;
BEGIN
 IF body IS NULL OR jsonb_typeof(body)<>'object' OR octet_length(body::text)>16384 OR
 EXISTS(SELECT FROM jsonb_object_keys(body) k WHERE k NOT IN ('email','fullName','roleId','entityId','phone','isActive','centerName')) OR
 coalesce(body->>'fullName','') !~ '[^[:space:]]' OR
 coalesce(body->>'email','') !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' OR
 coalesce(body->>'roleId','') NOT IN ('SUPER_ADMIN','ADMIN_CENTRO','TERAPEUTA','AUXILIAR') OR
 jsonb_typeof(body->'isActive') IS DISTINCT FROM 'boolean' OR
 EXISTS(SELECT FROM jsonb_each(body) e WHERE e.key<>'isActive' AND e.value<>'null'::jsonb AND
 (jsonb_typeof(e.value)<>'string' OR length(e.value #>> '{}')>1024)) THEN
 RAISE EXCEPTION 'INVALID_INPUT'; END IF;
 destination := (body->>'entityId')::uuid;
 SELECT * INTO actor FROM public.user_profiles WHERE id=actor_id FOR SHARE;
 IF actor.id IS NULL OR NOT actor.is_active OR actor.role_id NOT IN ('SUPER_ADMIN','ADMIN_CENTRO') THEN
 RAISE EXCEPTION 'FORBIDDEN'; END IF;
 IF body->>'roleId'='SUPER_ADMIN' THEN
 IF actor.role_id<>'SUPER_ADMIN' OR destination IS NOT NULL THEN RAISE EXCEPTION 'FORBIDDEN'; END IF;
 ELSE
 SELECT * INTO center FROM public.entities WHERE id=destination FOR SHARE;
 IF center.id IS NULL OR center.status IS DISTINCT FROM 'ACTIVE' OR
 (center.license_expires_at IS NOT NULL AND center.license_expires_at<=clock_timestamp()) THEN
 RAISE EXCEPTION 'INVALID_ASSOCIATION'; END IF;
 END IF;
 IF actor.role_id='ADMIN_CENTRO' AND (destination IS DISTINCT FROM actor.entity_id OR
 body->>'roleId' NOT IN ('TERAPEUTA','AUXILIAR')) THEN RAISE EXCEPTION 'FORBIDDEN'; END IF;
END $$;

CREATE FUNCTION public.admin_create_user_control(p_actor_id uuid,p_request_id uuid,p_body jsonb,p_mode text)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path='' SET lock_timeout='5s' AS $$
DECLARE prior admin_phase2_private.user_creations%ROWTYPE;
BEGIN
 IF p_actor_id IS NULL OR p_request_id IS NULL OR p_mode IS NULL OR p_mode NOT IN ('prepare','result','close') THEN
 RAISE EXCEPTION 'INVALID_INPUT'; END IF;
 PERFORM pg_advisory_xact_lock(hashtextextended('create-user:'||p_actor_id::text||p_request_id::text,0));
 PERFORM admin_phase2_private.authorize_creation(p_actor_id,p_body);
 SELECT * INTO prior FROM admin_phase2_private.user_creations WHERE actor_id=p_actor_id AND request_id=p_request_id FOR UPDATE;
 IF FOUND AND prior.body<>p_body THEN RAISE EXCEPTION 'IDEMPOTENCY_CONFLICT'; END IF;
 IF prior.state='COMPLETED' THEN RETURN jsonb_build_object('success',true,'code','COMPLETED','data',jsonb_build_object('userId',prior.user_id)); END IF;
 IF prior.state='CANCELLED' THEN RETURN jsonb_build_object('success',false,'code','CANCELLED'); END IF;
 IF p_mode='close' THEN
 INSERT INTO admin_phase2_private.user_creations(actor_id,request_id,body,state) VALUES(p_actor_id,p_request_id,p_body,'CANCELLED')
 ON CONFLICT(actor_id,request_id) DO UPDATE SET state='CANCELLED';
 RETURN jsonb_build_object('success',false,'code','CANCELLED');
 END IF;
 IF prior.actor_id IS NOT NULL OR p_mode='result' THEN RETURN jsonb_build_object('success',false,'code','NOT_OBSERVED'); END IF;
 INSERT INTO admin_phase2_private.user_creations(actor_id,request_id,body,state)
 VALUES(p_actor_id,p_request_id,p_body,'PENDING') RETURNING * INTO prior;
 RETURN jsonb_build_object('success',false,'code','PREPARED','ticket',prior.ticket);
END $$;

CREATE FUNCTION admin_phase2_private.finish_user_creation()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path='' SET lock_timeout='5s' AS $$
DECLARE item admin_phase2_private.user_creations%ROWTYPE; ticket_id uuid;
BEGIN
 -- app_metadata is set only by the Auth admin API, never user_metadata.
 IF NOT (coalesce(NEW.raw_app_meta_data,'{}'::jsonb) ? 'admin_creation_ticket') THEN RETURN NEW; END IF;
 ticket_id := (NEW.raw_app_meta_data->>'admin_creation_ticket')::uuid;
 SELECT * INTO item FROM admin_phase2_private.user_creations WHERE ticket=ticket_id;
 IF item.actor_id IS NULL THEN RAISE EXCEPTION 'INVALID_CREATION_TICKET'; END IF;
 PERFORM pg_advisory_xact_lock(hashtextextended('create-user:'||item.actor_id::text||item.request_id::text,0));
 SELECT * INTO item FROM admin_phase2_private.user_creations WHERE ticket=ticket_id FOR UPDATE;
 IF item.state<>'PENDING' OR item.expires_at<=clock_timestamp() OR
 lower(NEW.email) IS DISTINCT FROM lower(item.body->>'email') THEN RAISE EXCEPTION 'CANCELLED'; END IF;
 PERFORM admin_phase2_private.authorize_creation(item.actor_id,item.body);
 INSERT INTO public.user_profiles(id,email,full_name,role_id,entity_id,phone,is_active,center_name)
 VALUES(NEW.id,NEW.email,item.body->>'fullName',item.body->>'roleId',(item.body->>'entityId')::uuid,
 item.body->>'phone',(item.body->>'isActive')::boolean,item.body->>'centerName');
 UPDATE admin_phase2_private.user_creations SET state='COMPLETED',user_id=NEW.id WHERE ticket=ticket_id;
 INSERT INTO admin_phase2_private.audit(actor_id,request_id,operation,target_id,code)
 VALUES(item.actor_id,item.request_id,'admin-create-user',NEW.id,'COMPLETED');
 RETURN NEW;
END $$;
CREATE TRIGGER admin_finish_user_creation AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION admin_phase2_private.finish_user_creation();
REVOKE ALL ON FUNCTION admin_phase2_private.authorize_creation(uuid,jsonb),
 admin_phase2_private.finish_user_creation(),public.admin_create_user_control(uuid,uuid,jsonb,text)
 FROM PUBLIC,anon,authenticated,service_role;
GRANT EXECUTE ON FUNCTION public.admin_create_user_control(uuid,uuid,jsonb,text) TO service_role;
COMMIT;
