-- Synthetic fixtures only; always rollback. Requires installed create-user.sql.
BEGIN;

DO $test$
DECLARE actor uuid:=gen_random_uuid(); center uuid:=gen_random_uuid(); key uuid:=gen_random_uuid(); target uuid:=gen_random_uuid(); ticket uuid; body jsonb; outcome jsonb;
BEGIN
 INSERT INTO auth.users(id,email,raw_user_meta_data) VALUES(actor,actor::text||'@example.invalid','{"full_name":"Synthetic test"}');
 INSERT INTO public.user_profiles(id,email,full_name,role_id,is_active) VALUES(actor,actor::text||'@example.invalid','Synthetic actor','SUPER_ADMIN',true);
 INSERT INTO public.entities(id,name,cif,status) VALUES(center,'Synthetic center',center::text,'ACTIVE');
 body:=jsonb_build_object('email',target::text||'@example.invalid','fullName','Synthetic staff','roleId','TERAPEUTA','entityId',center,'isActive',true);
 outcome:=public.admin_create_user_control(actor,key,body,'prepare');
 ticket:=(outcome->>'ticket')::uuid;
 INSERT INTO auth.users(id,email,raw_app_meta_data,raw_user_meta_data) VALUES(target,body->>'email',jsonb_build_object('admin_creation_ticket',ticket),'{"full_name":"Synthetic staff"}');
 IF NOT EXISTS(SELECT FROM public.user_profiles WHERE id=target AND role_id='TERAPEUTA' AND entity_id=center) THEN RAISE EXCEPTION 'profile missing'; END IF;
 outcome:=public.admin_create_user_control(actor,key,body,'result');
 IF outcome->>'code'<>'COMPLETED' THEN RAISE EXCEPTION 'receipt missing'; END IF;
 IF public.admin_create_user_control(actor,key,body,'prepare')<>outcome THEN RAISE EXCEPTION 'idempotency failed'; END IF;
 -- A late Auth request after cancellation must roll back its identity.
 key:=gen_random_uuid();
 body:=jsonb_set(body,'{email}',to_jsonb(gen_random_uuid()::text||'@example.invalid'));
 ticket:=(public.admin_create_user_control(actor,key,body,'prepare')->>'ticket')::uuid;
 PERFORM public.admin_create_user_control(actor,key,body,'close');
 BEGIN
 INSERT INTO auth.users(id,email,raw_app_meta_data) VALUES(gen_random_uuid(),body->>'email',jsonb_build_object('admin_creation_ticket',ticket));
 RAISE EXCEPTION 'late insert accepted';
 EXCEPTION WHEN SQLSTATE 'P0001' THEN IF SQLERRM<>'CANCELLED' THEN RAISE; END IF;
 END;
 UPDATE public.user_profiles SET role_id='ADMIN_CENTRO',entity_id=center WHERE id=actor;
 body:=jsonb_set(body,'{roleId}','"SUPER_ADMIN"');
 BEGIN
 PERFORM public.admin_create_user_control(actor,gen_random_uuid(),body,'prepare');
 RAISE EXCEPTION 'escalation accepted';
 EXCEPTION WHEN SQLSTATE 'P0001' THEN IF SQLERRM<>'FORBIDDEN' THEN RAISE; END IF;
 END;
END $test$;

ROLLBACK;
