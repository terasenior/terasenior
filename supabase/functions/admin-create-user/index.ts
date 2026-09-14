import { createUserHandler } from "../_shared/admin-create-user.mjs";

Deno.serve(createUserHandler({
  url: Deno.env.get("SUPABASE_URL"),
  serverKey: Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"),
  allowedOrigins: (Deno.env.get("ADMIN_ALLOWED_ORIGINS") ?? "https://terasenior.es,https://www.terasenior.es,https://terasenior.github.io")
    .split(",").map(value => value.trim()).filter(Boolean),
}));
