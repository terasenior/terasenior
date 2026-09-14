BEGIN;
CREATE FUNCTION public.admin_create_user_finish(p_actor_id uuid,p_request_id uuid,p_user_id uuid)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path='' SET lock_timeout='5s' AS $$
DECLARE item admin_phase2_private.user_creations%ROWTYPE; auth_email text;
BEGIN
  IF p_actor_id IS NULL OR p_request_id IS NULL OR p_user_id IS NULL THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_INPUT';
  END IF;
  PERFORM pg_advisory_xact_lock(hashtextextended('create-user:'||p_actor_id::text||p_request_id::text,0));
  SELECT * INTO item FROM admin_phase2_private.user_creations
    WHERE actor_id=p_actor_id AND request_id=p_request_id FOR UPDATE;
  IF item.actor_id IS NULL OR item.state<>'PENDING' OR item.expires_at<=clock_timestamp() THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='CANCELLED';
  END IF;
  PERFORM admin_phase2_private.authorize_creation(p_actor_id,item.body);
  SELECT email INTO auth_email FROM auth.users WHERE id=p_user_id FOR SHARE;
  IF auth_email IS NULL OR lower(auth_email) IS DISTINCT FROM lower(item.body->>'email') THEN
    RAISE EXCEPTION USING ERRCODE='P0001', MESSAGE='INVALID_ASSOCIATION';
  END IF;
  INSERT INTO public.user_profiles(id,email,full_name,role_id,entity_id,phone,is_active,center_name)
  VALUES(p_user_id,auth_email,item.body->>'fullName',item.body->>'roleId',
    (item.body->>'entityId')::uuid,item.body->>'phone',(item.body->>'isActive')::boolean,
    item.body->>'centerName');
  UPDATE admin_phase2_private.user_creations SET state='COMPLETED',user_id=p_user_id
    WHERE actor_id=p_actor_id AND request_id=p_request_id;
  INSERT INTO admin_phase2_private.audit(actor_id,request_id,operation,target_id,code)
    VALUES(p_actor_id,p_request_id,'admin-create-user',p_user_id,'COMPLETED');
  RETURN jsonb_build_object('success',true,'code','COMPLETED','data',jsonb_build_object('userId',p_user_id));
END $$;
REVOKE ALL ON FUNCTION public.admin_create_user_finish(uuid,uuid,uuid)
  FROM PUBLIC,anon,authenticated,service_role;
GRANT EXECUTE ON FUNCTION public.admin_create_user_finish(uuid,uuid,uuid) TO service_role;
COMMIT;
