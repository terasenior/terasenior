// Local preparation only. No Auth/PostgREST writes until a verified atomic backend exists.
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const roles = ["SUPER_ADMIN", "ADMIN_CENTRO", "TERAPEUTA", "AUXILIAR"];
const staff = ["TERAPEUTA", "AUXILIAR"];
const fields = {
  "admin-create-user": ["email", "password", "fullName", "roleId", "entityId", "phone", "isActive", "centerName"],
  "admin-update-user": ["targetUserId", "email", "fullName", "phone", "roleId", "entityId", "isActive", "centerName", "clearPhone", "clearCenterName"],
  "admin-delete-user": ["targetUserId"],
  "admin-create-entity": ["name", "cif", "address", "licenseExpiresAt", "logoUrl"],
  "admin-set-entity-status": ["entityId", "status"],
  "admin-set-entity-license": ["entityId", "licenseExpiresAt"],
  "admin-delete-entity": ["entityId"],
};
class Rejection extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
const reject = (status, code) => { throw new Rejection(status, code); };
const object = (v) => v !== null && typeof v === "object" && !Array.isArray(v);
const id = (v) => typeof v === "string" && uuid.test(v);
const text = (v) => typeof v === "string" && v.trim().length > 0 && v.length <= 1024;

export function validate(operation, body) {
  if (!fields[operation] || !object(body) || Object.keys(body).some(k => !fields[operation].includes(k))) reject(400, "INVALID_INPUT");
  for (const key of ["email", "fullName", "phone", "centerName", "name", "cif", "address", "logoUrl"]) {
    if (body[key] != null && (typeof body[key] !== "string" || body[key].length > 1024)) reject(400, "INVALID_INPUT");
  }
  if (body.email != null && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email)) reject(400, "INVALID_INPUT");
  for (const key of ["isActive", "clearPhone", "clearCenterName"]) {
    if (key in body && body[key] !== null && typeof body[key] !== "boolean") reject(400, "INVALID_INPUT");
  }
  if (body.roleId != null && !roles.includes(body.roleId)) reject(400, "INVALID_INPUT");
  if (body.clearPhone && body.phone != null || body.clearCenterName && body.centerName != null) reject(400, "INVALID_INPUT");
  if (operation === "admin-update-user") {
    const update = body.entityId;
    if (update != null && (!object(update) || Object.keys(update).some(k => !["action", "value"].includes(k)) ||
      !(update.action === "SET" && id(update.value) || update.action === "CLEAR" && update.value == null))) reject(400, "INVALID_INPUT");
  } else if (body.entityId != null && !id(body.entityId)) reject(400, "INVALID_INPUT");
  if (["admin-update-user", "admin-delete-user"].includes(operation) && !id(body.targetUserId)) reject(400, "INVALID_INPUT");
  if (["admin-set-entity-status", "admin-set-entity-license", "admin-delete-entity"].includes(operation) && !id(body.entityId)) reject(400, "INVALID_INPUT");
  if (operation === "admin-set-entity-status" && !["ACTIVE", "INACTIVE"].includes(body.status)) reject(400, "INVALID_INPUT");
  if (operation === "admin-set-entity-license" && !("licenseExpiresAt" in body)) reject(400, "INVALID_INPUT");
  if (body.licenseExpiresAt != null && (typeof body.licenseExpiresAt !== "string" ||
    !/^\d{4}-\d{2}-\d{2}(?:T.*)?$/.test(body.licenseExpiresAt) || !Number.isFinite(Date.parse(body.licenseExpiresAt)))) reject(400, "INVALID_INPUT");
  if (operation === "admin-create-user" && (!text(body.fullName) || !text(body.email) || !roles.includes(body.roleId) ||
    typeof body.isActive !== "boolean" || typeof body.password !== "string" || body.password.length < 6 || body.password.length > 1024)) reject(400, "INVALID_INPUT");
  if (operation === "admin-create-entity" && (!text(body.name) || !text(body.cif))) reject(400, "INVALID_INPUT");
  return body;
}

export function authorize(operation, body, actor, target, center, now = Date.now()) {
  if (!actor || actor.is_active !== true || !["SUPER_ADMIN", "ADMIN_CENTRO"].includes(actor.role_id)) reject(403, "FORBIDDEN");
  if (body.targetUserId && (!target || target.id !== body.targetUserId)) reject(403, "FORBIDDEN");
  // Administrative self-edits/deletion are forbidden; use a separate self-service flow.
  if (body.targetUserId === actor.id) reject(403, "SELF_OPERATION_FORBIDDEN");
  if (actor.role_id === "SUPER_ADMIN") return;
  if (!operation.endsWith("-user") || !actor.entity_id || !center || center.id !== actor.entity_id || center.status !== "ACTIVE" ||
    !(center.license_expires_at === null || typeof center.license_expires_at === "string" && Date.parse(center.license_expires_at) > now)) reject(403, "FORBIDDEN");
  if (target && (target.entity_id !== actor.entity_id || !staff.includes(target.role_id))) reject(403, "FORBIDDEN");
  if (body.roleId != null && !staff.includes(body.roleId)) reject(403, "FORBIDDEN");
  if (operation === "admin-create-user" && body.entityId !== actor.entity_id) reject(403, "FORBIDDEN");
  if (operation === "admin-update-user" && body.entityId != null &&
    (body.entityId.action !== "SET" || body.entityId.value !== actor.entity_id)) reject(403, "FORBIDDEN");
}

async function readBody(request) {
  const reader = request.body?.getReader();
  if (!reader) reject(400, "INVALID_INPUT");
  const chunks = []; let size = 0;
  while (true) {
    const { value, done } = await reader.read(); if (done) break;
    size += value.byteLength;
    if (size > 16384) { await reader.cancel(); reject(413, "BODY_TOO_LARGE"); }
    chunks.push(value);
  }
  const bytes = new Uint8Array(size); let offset = 0;
  for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
  try { return JSON.parse(new TextDecoder().decode(bytes)); } catch { reject(400, "INVALID_INPUT"); }
}

export function createHandler(operation, { url, serverKey, allowedOrigins = [], fetchImpl = fetch } = {}) {
  if (!fields[operation]) throw new Error("Unknown administrative operation");
  return async request => {
    const origin = request.headers.get("origin");
    const headers = { "Content-Type": "application/json", "Cache-Control": "no-store", Vary: "Origin" };
    if (origin && allowedOrigins.includes(origin)) Object.assign(headers, {
      "Access-Control-Allow-Origin": origin, "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
    });
    const response = (status, code) => new Response(JSON.stringify({ success: false, code,
      message: code === "ATOMIC_BACKEND_REQUIRED" ? "Operación pendiente de backend transaccional verificado. No se ha escrito ningún cambio." : "No se pudo completar la operación administrativa." }), { status, headers });
    try {
      if (origin && !allowedOrigins.includes(origin)) reject(403, "FORBIDDEN_ORIGIN");
      if (request.method === "OPTIONS") return new Response(null, { status: 204, headers });
      if (request.method !== "POST") reject(405, "METHOD_NOT_ALLOWED");
      if (!url || !serverKey || !url.startsWith("https://")) reject(503, "UNAVAILABLE");
      const token = request.headers.get("authorization") ?? "";
      if (!/^Bearer \S+$/i.test(token)) reject(401, "UNAUTHENTICATED");
      if (request.headers.get("content-type")?.split(";")[0].trim() !== "application/json") reject(400, "INVALID_INPUT");
      const body = validate(operation, await readBody(request));
      const call = async (path, authorization) => {
        const result = await fetchImpl(`${url.replace(/\/$/, "")}${path}`, {
          method: "GET", redirect: "error", signal: AbortSignal.timeout(10000),
          headers: { apikey: serverKey, Authorization: authorization },
        });
        if (!result.ok) reject(path === "/auth/v1/user" && [401, 403].includes(result.status) ? 401 : 502, "LOOKUP_FAILED");
        return result.json();
      };
      const identity = await call("/auth/v1/user", token);
      if (!id(identity?.id)) reject(401, "UNAUTHENTICATED");
      const read = async (table, rowId, columns) => {
        if (!id(rowId)) reject(403, "FORBIDDEN");
        const rows = await call(`/rest/v1/${table}?id=eq.${encodeURIComponent(rowId)}&select=${columns}`, `Bearer ${serverKey}`);
        return Array.isArray(rows) && rows.length === 1 && rows[0].id === rowId ? rows[0] : null;
      };
      const columns = "id,role_id,entity_id,is_active";
      const actor = await read("user_profiles", identity.id, columns);
      if (!actor || actor.is_active !== true || !["SUPER_ADMIN", "ADMIN_CENTRO"].includes(actor.role_id)) reject(403, "FORBIDDEN");
      const target = body.targetUserId ? await read("user_profiles", body.targetUserId, columns) : null;
      const center = actor.role_id === "ADMIN_CENTRO" ? await read("entities", actor.entity_id, "id,status,license_expires_at") : null;
      authorize(operation, body, actor, target, center);
      // No unsafe compensation, separate Auth/profile calls, or unverified cascade.
      // A future transaction must re-check actor/target permissions under locks.
      return response(503, "ATOMIC_BACKEND_REQUIRED");
    } catch (error) {
      return response(error instanceof Rejection ? error.status : 502, error instanceof Rejection ? error.code : "UPSTREAM_FAILURE");
    }
  };
}
