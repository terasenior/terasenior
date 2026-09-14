-- Synthetic smoke test, authorized development project only. Always rolls back.
BEGIN;
DO $$
DECLARE actor uuid := gen_random_uuid(); target uuid := gen_random_uuid(); center uuid := gen_random_uuid();
BEGIN
  PERFORM set_config('phase2.actor',actor::text,true);
  PERFORM set_config('phase2.target',target::text,true);
  PERFORM set_config('phase2.center',center::text,true);
  INSERT INTO public.entities(id,name,cif,status) VALUES(center,'Synthetic rollback center','SYNTHETIC','ACTIVE');
  INSERT INTO auth.users(id,email,raw_user_meta_data) VALUES
    (actor,actor::text||'@example.invalid','{"full_name":"Synthetic actor"}'),
    (target,target::text||'@example.invalid','{"full_name":"Synthetic target"}');
  INSERT INTO public.user_profiles(id,entity_id,role_id,full_name,email,is_active) VALUES
    (actor,center,'SUPER_ADMIN','Synthetic actor',actor::text||'@example.invalid',true),
    (target,center,'TERAPEUTA','Synthetic target',target::text||'@example.invalid',true);
  IF has_function_privilege('anon','public.admin_phase2_execute(uuid,uuid,text,jsonb)','EXECUTE') OR
     has_function_privilege('authenticated','public.admin_phase2_execute(uuid,uuid,text,jsonb)','EXECUTE') THEN
    RAISE EXCEPTION 'Client has privileged RPC permission';
  END IF;
END $$;
SET LOCAL ROLE service_role;
DO $$
DECLARE
  actor uuid := current_setting('phase2.actor')::uuid;
  target uuid := current_setting('phase2.target')::uuid;
  key uuid := gen_random_uuid(); close_key uuid := gen_random_uuid();
  body jsonb := '{"name":"Synthetic created center","cif":"SYNTHETIC"}';
  result jsonb; repeated jsonb;
BEGIN
  result := public.admin_phase2_execute(actor,key,'admin-create-entity',body);
  IF result->>'success' IS DISTINCT FROM 'true' THEN RAISE EXCEPTION 'Create failed: %',result; END IF;
  repeated := public.admin_phase2_execute(actor,key,'admin-create-entity',body);
  IF result IS DISTINCT FROM repeated THEN RAISE EXCEPTION 'Idempotency mismatch'; END IF;
  IF public.admin_phase2_result(actor,key,'admin-create-entity',body) IS DISTINCT FROM result THEN
    RAISE EXCEPTION 'Receipt lookup mismatch';
  END IF;
  result := public.admin_phase2_execute(actor,gen_random_uuid(),'admin-update-user',
    jsonb_build_object('targetUserId',target,'fullName','Synthetic updated','email',target::text||'@example.invalid'));
  IF result->>'success' IS DISTINCT FROM 'true' THEN RAISE EXCEPTION 'Update failed: %',result; END IF;
  result := public.admin_phase2_execute(actor,gen_random_uuid(),'admin-update-user',
    jsonb_build_object('targetUserId',target,'fullName','Must not persist','email','changed@example.invalid'));
  IF result->>'code' IS DISTINCT FROM 'EMAIL_CHANGE_UNAVAILABLE' THEN RAISE EXCEPTION 'Email rejection failed'; END IF;
  result := public.admin_phase2_close(actor,close_key,'admin-create-entity',body);
  IF result->>'code' IS DISTINCT FROM 'CANCELLED' THEN RAISE EXCEPTION 'Close failed'; END IF;
  result := public.admin_phase2_execute(actor,close_key,'admin-create-entity',body);
  IF result->>'code' IS DISTINCT FROM 'CANCELLED' THEN RAISE EXCEPTION 'Late execution not blocked'; END IF;
  BEGIN
    PERFORM public.admin_phase2_execute(target,gen_random_uuid(),'admin-create-entity',body);
    RAISE EXCEPTION 'Staff unexpectedly authorized';
  EXCEPTION WHEN SQLSTATE 'P0001' THEN
    IF SQLERRM <> 'FORBIDDEN' THEN RAISE; END IF;
  END;
END $$;
RESET ROLE;
DO $$
BEGIN
  IF (SELECT full_name FROM public.user_profiles WHERE id=current_setting('phase2.target')::uuid)
     IS DISTINCT FROM 'Synthetic updated' THEN RAISE EXCEPTION 'Partial email patch detected'; END IF;
  IF (SELECT count(*) FROM admin_phase2_private.audit WHERE actor_id=current_setting('phase2.actor')::uuid) <> 3 THEN
    RAISE EXCEPTION 'Audit count incorrect';
  END IF;
END $$;
ROLLBACK;
SELECT 'PASS: create/update, idempotency/result/close, email rejection, staff denial, client grants; synthetic rows rolled back' AS smoke_result;
