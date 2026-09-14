import { createDeployedHandler } from "../_shared/admin-bounded-transport.mjs";

Deno.serve(createDeployedHandler("admin-create-entity", {
  url: Deno.env.get("SUPABASE_URL"),
  serverKey: Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"),
  allowedOrigins: (Deno.env.get("ADMIN_ALLOWED_ORIGINS") ?? "https://terasenior.es,https://terasenior.github.io")
    .split(",").map(value => value.trim()).filter(Boolean),
}));
