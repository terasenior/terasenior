create table public.patient_occupational_followups (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid not null references public.patients(id) on delete cascade,
  author_id uuid not null references auth.users(id),
  author_name text not null,
  author_role text not null,
  followup_date date not null,
  neuropsychological_indications text,
  treatment_objectives text,
  intervention text,
  observations text,
  signature_name text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index patient_occupational_followups_patient_date_idx
  on public.patient_occupational_followups(patient_id, followup_date desc);

alter table public.patient_occupational_followups enable row level security;

create policy patient_occupational_followups_select_authorized on public.patient_occupational_followups for select to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_occupational_followups.patient_id and public.can_read_entity(p.entity_id))
);

create policy patient_occupational_followups_insert_authorized on public.patient_occupational_followups for insert to authenticated with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_occupational_followups.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_occupational_followups_update_authorized on public.patient_occupational_followups for update to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_occupational_followups.patient_id and public.can_write_clinical_entity(p.entity_id))
) with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_occupational_followups.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_occupational_followups_delete_authorized on public.patient_occupational_followups for delete to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_occupational_followups.patient_id and public.can_admin_entity(p.entity_id))
);
