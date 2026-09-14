import { createHandler } from "../_shared/admin-phase2.mjs";

Deno.serve(createHandler("admin-set-entity-status", {
  url: Deno.env.get("SUPABASE_URL"),
  serverKey: Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"),
  allowedOrigins: (Deno.env.get("ADMIN_ALLOWED_ORIGINS") ?? "")
    .split(",").map(value => value.trim()).filter(Boolean),
}));
