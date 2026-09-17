+create table if not exists public.patient_assessment_changes (
  id uuid primary key default gen_random_uuid(),
  assessment_id uuid not null references public.patient_assessments(id) on delete cascade,
  action text not null check (action in ('CREATED','UPDATED','DISCONTINUED')),
  professional_name text not null,
  professional_role text not null,
  changed_at timestamptz not null default now(),
  snapshot jsonb not null
);
create index if not exists patient_assessment_changes_assessment_changed_idx on public.patient_assessment_changes(assessment_id, changed_at desc);
alter table public.patient_assessment_changes enable row level security;
drop policy if exists patient_assessment_changes_select_authorized on public.patient_assessment_changes;
create policy patient_assessment_changes_select_authorized on public.patient_assessment_changes for select to authenticated using (exists (select 1 from public.patient_assessments a join public.patients p on p.id=a.patient_id where a.id=patient_assessment_changes.assessment_id and public.can_read_entity(p.entity_id)));
create or replace function public.patient_assessments_set_metadata()
returns trigger language plpgsql as $$
declare actor public.user_profiles%rowtype;
declare event_action text;
begin
  select * into actor from public.user_profiles where id = auth.uid();
  if tg_op = 'INSERT' then
    new.author_id := auth.uid();
    new.author_name := coalesce(actor.full_name, 'Profesional');
    new.author_role := coalesce(actor.role_id, 'TERAPEUTA');
    event_action := 'CREATED';
  else
    new.author_id := old.author_id; new.author_name := old.author_name; new.author_role := old.author_role; new.created_at := old.created_at; new.updated_at := now();
    new.updated_by_name := coalesce(actor.full_name, old.updated_by_name, old.author_name); new.updated_by_role := coalesce(actor.role_id, old.updated_by_role, old.author_role);
    event_action := 'UPDATED';
    if new.status = 'DISCONTINUED' and old.status <> 'DISCONTINUED' then new.discontinued_at := now(); new.discontinued_by_name := coalesce(actor.full_name, old.author_name); event_action := 'DISCONTINUED'; end if;
  end if;
  insert into public.patient_assessment_changes(assessment_id, action, professional_name, professional_role, snapshot)
  values (new.id, event_action, coalesce(actor.full_name, new.author_name), coalesce(actor.role_id, new.author_role), to_jsonb(new));
  return new;
end;
$$;
