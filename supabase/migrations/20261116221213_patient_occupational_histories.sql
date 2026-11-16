create table public.patient_occupational_histories (
  patient_id uuid primary key references public.patients(id) on delete cascade,
  author_id uuid not null references auth.users(id),
  author_name text not null,
  author_role text not null,
  referral_reason text,
  medical_diagnosis text,
  relevant_health_history text,
  medication text,
  functional_assessment text,
  cognitive_emotional_status text,
  sensory_communication text,
  housing_and_support text,
  family_social_context text,
  daily_routines text,
  occupational_history text,
  interests_preferences text,
  therapeutic_goals text,
  intervention_plan text,
  observations text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.patient_occupational_histories enable row level security;

create policy patient_occupational_histories_select_authorized on public.patient_occupational_histories for select to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_occupational_histories.patient_id and public.can_read_entity(p.entity_id))
);

create policy patient_occupational_histories_insert_authorized on public.patient_occupational_histories for insert to authenticated with check (
  exists (select 1 from public.patients p where p.id = patient_occupational_histories.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_occupational_histories_update_authorized on public.patient_occupational_histories for update to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_occupational_histories.patient_id and public.can_write_clinical_entity(p.entity_id))
) with check (
  exists (select 1 from public.patients p where p.id = patient_occupational_histories.patient_id and public.can_write_clinical_entity(p.entity_id))
);

create policy patient_occupational_histories_delete_authorized on public.patient_occupational_histories for delete to authenticated using (
  exists (select 1 from public.patients p where p.id = patient_occupational_histories.patient_id and public.can_admin_entity(p.entity_id))
);
