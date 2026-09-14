// No SDK dependencies. Tests inject a fake fetch; this module reads no secrets.
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function canChangePassword(caller, target, entity, now = Date.now()) {
  if (!caller || !target || caller.is_active !== true || caller.id === target.id) return false;
  if (caller.role_id === "SUPER_ADMIN") return true;
  if (caller.role_id !== "ADMIN_CENTRO" || !caller.entity_id ||
      caller.entity_id !== target.entity_id ||
      !["TERAPEUTA", "AUXILIAR"].includes(target.role_id)) return false;
  if (!entity || entity.id !== caller.entity_id || entity.status !== "ACTIVE") return false;
  return entity.license_expires_at === null ||
    (typeof entity.license_expires_at === "string" && Date.parse(entity.license_expires_at) > now);
}

export function createHandler({ url, serverKey, allowedOrigins = [], fetchImpl = fetch }) {
  return async (request) => {
    const origin = request.headers.get("origin");
    const headers = { "Cache-Control": "no-store", "Vary": "Origin" };
    if (origin && allowedOrigins.includes(origin)) {
      headers["Access-Control-Allow-Origin"] = origin;
      headers["Access-Control-Allow-Headers"] = "authorization, apikey, content-type, x-client-info";
      headers["Access-Control-Allow-Methods"] = "POST, OPTIONS";
    }
    const respond = (status) => new Response(null, { status, headers });
    if (origin && !allowedOrigins.includes(origin)) return respond(403);
    if (request.method === "OPTIONS") return respond(204);
    if (request.method !== "POST") return respond(405);
    if (!url || !serverKey || !url.startsWith("https://")) return respond(503);
    const authorization = request.headers.get("authorization") ?? "";
    if (!/^Bearer \S+$/i.test(authorization)) return respond(401);
    if (request.headers.get("content-type")?.split(";")[0].trim() !== "application/json") return respond(400);

    try {
      // Bound actual bytes, including chunked requests, before decoding the body.
      const reader = request.body?.getReader();
      if (!reader) return respond(400);
      const chunks = [];
      let size = 0;
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        size += value.byteLength;
        if (size > 8192) { await reader.cancel(); return respond(413); }
        chunks.push(value);
      }
      const bytes = new Uint8Array(size);
      let offset = 0;
      for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
      let body;
      try { body = JSON.parse(new TextDecoder().decode(bytes)); } catch { return respond(400); }
      if (!body || Array.isArray(body) ||
          Object.keys(body).some((key) => !["targetUserId", "newPassword"].includes(key)) ||
          typeof body.targetUserId !== "string" || !uuid.test(body.targetUserId) ||
          typeof body.newPassword !== "string" || body.newPassword.length < 6 ||
          body.newPassword.length > 1024) return respond(400);

      const base = url.replace(/\/$/, "");
      const call = (path, options = {}) => fetchImpl(`${base}${path}`, {
        ...options, redirect: "error", signal: AbortSignal.timeout(10_000),
      });
      // Verify the caller token with Auth; never trust identity or roles from the body.
      const identity = await call("/auth/v1/user", {
        headers: { apikey: serverKey, Authorization: authorization },
      });
      if (!identity.ok) return respond(identity.status === 401 || identity.status === 403 ? 401 : 502);
      const user = await identity.json();
      if (!user || !uuid.test(user.id ?? "")) return respond(401);
      if (user.id === body.targetUserId) return respond(403);

      const adminHeaders = { apikey: serverKey, Authorization: `Bearer ${serverKey}` };
      const readOne = async (table, id, columns) => {
        const response = await call(`/rest/v1/${table}?id=eq.${encodeURIComponent(id)}&select=${columns}`, {
          headers: adminHeaders,
        });
        if (!response.ok) throw new Error("Profile lookup failed");
        const rows = await response.json();
        return Array.isArray(rows) && rows.length === 1 && rows[0].id === id ? rows[0] : null;
      };
      const columns = "id,role_id,entity_id,is_active";
      const caller = await readOne("user_profiles", user.id, columns);
      if (!caller || caller.is_active !== true ||
          !["SUPER_ADMIN", "ADMIN_CENTRO"].includes(caller.role_id)) return respond(403);
      const target = await readOne("user_profiles", body.targetUserId, columns);
      const entity = caller.role_id === "ADMIN_CENTRO" && caller.entity_id
        ? await readOne("entities", caller.entity_id, "id,status,license_expires_at") : null;
      if (!canChangePassword(caller, target, entity)) return respond(403);

      // The sole write: Auth Admin update by destination ID. No profile/RLS/schema writes.
      const result = await call(`/auth/v1/admin/users/${body.targetUserId}`, {
        method: "PUT",
        headers: { ...adminHeaders, "Content-Type": "application/json" },
        body: JSON.stringify({ password: body.newPassword }),
      });
      if (result.ok) return respond(204);
      if ([400, 422, 429].includes(result.status)) return respond(result.status);
      return respond(502);
    } catch {
      // Never log request bodies, passwords, tokens or upstream error responses.
      return respond(502);
    }
  };
}
