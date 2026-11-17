create table public.patient_manual_activity_logs (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid not null references public.patients(id) on delete cascade,
  author_id uuid not null references auth.users(id),
  author_name text not null,
  author_role text not null,
  activity_date date not null,
  activity_name text not null,
  therapeutic_area text,
  complexity text not null default 'MEDIA' check (complexity in ('BAJA', 'MEDIA', 'ALTA')),
  performance_score integer check (performance_score between 0 and 4),
  duration_minutes integer check (duration_minutes is null or duration_minutes >= 0),
  result_notes text,
  behavioral_observations text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index patient_manual_activity_logs_patient_date_idx
  on public.patient_manual_activity_logs(patient_id, activity_date desc);

alter table public.patient_manual_activity_logs enable row level security;

create policy patient_manual_activity_logs_select_authorized on public.patient_manual_activity_logs for select to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_manual_activity_logs.patient_id and public.can_read_entity(p.entity_id))
);

create policy patient_manual_activity_logs_insert_authorized on public.patient_manual_activity_logs for insert to authenticated with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_manual_activity_logs.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_manual_activity_logs_update_authorized on public.patient_manual_activity_logs for update to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_manual_activity_logs.patient_id and public.can_write_clinical_entity(p.entity_id))
) with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_manual_activity_logs.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_manual_activity_logs_delete_authorized on public.patient_manual_activity_logs for delete to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_manual_activity_logs.patient_id and public.can_admin_entity(p.entity_id))
);
