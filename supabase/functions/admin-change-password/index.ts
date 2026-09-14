import { createHandler } from "./handler.mjs";

// Read only in the deployed server runtime. Do not create a local secret file.
Deno.serve(createHandler({
  url: Deno.env.get("SUPABASE_URL"),
  serverKey: Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"),
  allowedOrigins: (Deno.env.get("ADMIN_PASSWORD_ALLOWED_ORIGINS") ?? "")
    .split(",").map((origin) => origin.trim()).filter(Boolean),
}));
