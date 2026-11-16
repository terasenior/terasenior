-- Applied in production as migration 20261116180336.
create table public.patient_discharges (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid not null references public.patients(id) on delete cascade,
  author_id uuid not null references auth.users(id),
  author_name text not null,
  author_role text not null,
  assessment_date date,
  service_arrival_date date,
  intervention_start_date date,
  discharge_date date not null,
  program_duration text,
  program_follow_up text,
  discharge_reason text not null,
  observations text,
  is_voluntary boolean not null default false,
  voluntary_declarant_name text,
  voluntary_declarant_document_id text,
  voluntary_declarant_relationship text,
  voluntary_declaration_date date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint patient_discharges_dates_order check (
    (assessment_date is null or service_arrival_date is null or assessment_date <= service_arrival_date)
    and (service_arrival_date is null or intervention_start_date is null or service_arrival_date <= intervention_start_date)
    and (intervention_start_date is null or intervention_start_date <= discharge_date)
  ),
  constraint patient_discharges_voluntary_details check (
    not is_voluntary or (
      nullif(trim(voluntary_declarant_name), '') is not null
      and nullif(trim(voluntary_declarant_document_id), '') is not null
      and nullif(trim(voluntary_declarant_relationship), '') is not null
      and voluntary_declaration_date is not null
    )
  )
);

create index patient_discharges_patient_discharge_date_idx
  on public.patient_discharges(patient_id, discharge_date desc);

alter table public.patient_discharges enable row level security;

create policy patient_discharges_select_authorized on public.patient_discharges for select to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_discharges.patient_id and public.can_read_entity(p.entity_id))
);

create policy patient_discharges_insert_authorized on public.patient_discharges for insert to authenticated with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_discharges.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_discharges_update_authorized on public.patient_discharges for update to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_discharges.patient_id and public.can_write_clinical_entity(p.entity_id))
) with check (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_discharges.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_discharges_delete_authorized on public.patient_discharges for delete to authenticated using (
  author_id = (select auth.uid())
  and exists (select 1 from public.patients p where p.id = patient_discharges.patient_id and public.can_admin_entity(p.entity_id))
);
