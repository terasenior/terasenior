+create table if not exists public.patient_assessments (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid not null references public.patients(id) on delete restrict,
  author_id uuid not null references public.user_profiles(id),
  author_name text not null,
  author_role text not null,
  mobility text,
  basic_activities text,
  instrumental_activities text,
  cognitive_status text,
  emotional_status text,
  risks text,
  decision_capacity text,
  status text not null default 'ACTIVE' check (status in ('ACTIVE','DISCONTINUED')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  updated_by_name text,
  updated_by_role text,
  discontinued_at timestamptz,
  discontinued_by_name text
);
create index if not exists patient_assessments_patient_created_idx on public.patient_assessments(patient_id, created_at desc);
alter table public.patient_assessments enable row level security;
create or replace function public.patient_assessments_set_metadata()
returns trigger language plpgsql as $$
declare actor public.user_profiles%rowtype;
begin
  select * into actor from public.user_profiles where id = auth.uid();
  if tg_op = 'INSERT' then
    new.author_id := auth.uid();
    new.author_name := coalesce(actor.full_name, 'Profesional');
    new.author_role := coalesce(actor.role_id, 'TERAPEUTA');
  else
    new.author_id := old.author_id;
    new.author_name := old.author_name;
    new.author_role := old.author_role;
    new.created_at := old.created_at;
    new.updated_at := now();
    new.updated_by_name := coalesce(actor.full_name, old.updated_by_name, old.author_name);
    new.updated_by_role := coalesce(actor.role_id, old.updated_by_role, old.author_role);
    if new.status = 'DISCONTINUED' and old.status <> 'DISCONTINUED' then
      new.discontinued_at := now();
      new.discontinued_by_name := coalesce(actor.full_name, old.author_name);
    end if;
  end if;
  return new;
end;
$$;
drop trigger if exists patient_assessments_metadata on public.patient_assessments;
create trigger patient_assessments_metadata before insert or update on public.patient_assessments for each row execute function public.patient_assessments_set_metadata();
drop policy if exists patient_assessments_select_authorized on public.patient_assessments;
drop policy if exists patient_assessments_insert_authorized on public.patient_assessments;
drop policy if exists patient_assessments_update_authorized on public.patient_assessments;
create policy patient_assessments_select_authorized on public.patient_assessments for select to authenticated using (exists (select 1 from public.patients p where p.id = patient_assessments.patient_id and public.can_read_entity(p.entity_id)));
create policy patient_assessments_insert_authorized on public.patient_assessments for insert to authenticated with check (exists (select 1 from public.patients p where p.id = patient_assessments.patient_id and public.can_write_clinical_entity(p.entity_id)));
create policy patient_assessments_update_authorized on public.patient_assessments for update to authenticated using (exists (select 1 from public.patients p where p.id = patient_assessments.patient_id and public.can_write_clinical_entity(p.entity_id))) with check (exists (select 1 from public.patients p where p.id = patient_assessments.patient_id and public.can_write_clinical_entity(p.entity_id)));
