import test from "node:test";
import assert from "node:assert/strict";
import { authorize, validate, createHandler } from "./admin-phase2.mjs";

const actorId = "11111111-1111-4111-8111-111111111111";
const targetId = "22222222-2222-4222-8222-222222222222";
const centerId = "33333333-3333-4333-8333-333333333333";
const otherId = "44444444-4444-4444-8444-444444444444";
const actor = { id: actorId, role_id: "ADMIN_CENTRO", entity_id: centerId, is_active: true };
const target = { id: targetId, role_id: "TERAPEUTA", entity_id: centerId, is_active: true };
const center = { id: centerId, status: "ACTIVE", license_expires_at: null };
const bodies = {
  "admin-create-user": { email: "test@example.invalid", password: "synthetic-password", fullName: "Test", roleId: "TERAPEUTA", entityId: centerId, isActive: true },
  "admin-update-user": { targetUserId: targetId, fullName: "Updated" },
  "admin-delete-user": { targetUserId: targetId },
  "admin-create-entity": { name: "Center", cif: "CIF", address: null, logoUrl: null, licenseExpiresAt: null },
  "admin-set-entity-status": { entityId: centerId, status: "INACTIVE" },
  "admin-set-entity-license": { entityId: centerId, licenseExpiresAt: null },
  "admin-delete-entity": { entityId: centerId },
};
const denied = fn => assert.throws(fn, error => error.status === 403);

test("global admin permits all operations except self operations", () => {
  const global = { ...actor, role_id: "SUPER_ADMIN" };
  for (const [op, body] of Object.entries(bodies)) assert.doesNotThrow(() => authorize(op, body, global, target, null));
  for (const op of ["admin-update-user", "admin-delete-user"]) denied(() => authorize(op, { targetUserId: actorId }, global, global, null));
});
test("center admin manages only local staff and cannot manage entities", () => {
  for (const [op, body] of Object.entries(bodies)) {
    if (op.endsWith("-user")) assert.doesNotThrow(() => authorize(op, body, actor, target, center));
    else denied(() => authorize(op, body, actor, target, center));
  }
  for (const role of ["SUPER_ADMIN", "ADMIN_CENTRO", "UNKNOWN"]) denied(() => authorize("admin-delete-user", bodies["admin-delete-user"], actor, { ...target, role_id: role }, center));
  denied(() => authorize("admin-delete-user", bodies["admin-delete-user"], actor, { ...target, entity_id: otherId }, center));
});
test("requested roles and associations cannot elevate or transfer privileges", () => {
  for (const roleId of ["SUPER_ADMIN", "ADMIN_CENTRO"]) denied(() => authorize("admin-update-user", { targetUserId: targetId, roleId }, actor, target, center));
  for (const entityId of [{ action: "CLEAR" }, { action: "SET", value: otherId }]) denied(() => authorize("admin-update-user", { targetUserId: targetId, entityId }, actor, target, center));
  denied(() => authorize("admin-create-user", { ...bodies["admin-create-user"], entityId: otherId }, actor, null, center));
  for (const entityId of [undefined, null, { action: "SET", value: centerId }]) assert.doesNotThrow(() => authorize("admin-update-user", { targetUserId: targetId, entityId }, actor, target, center));
});
test("inactive actors and inactive, expired or missing centers fail closed", () => {
  for (const invalid of [null, { ...center, status: "INACTIVE" }, { ...center, license_expires_at: "2000-01-01" }, { ...center, license_expires_at: "invalid" }]) denied(() => authorize("admin-delete-user", bodies["admin-delete-user"], actor, target, invalid));
  for (const invalid of [null, { ...actor, is_active: false }, { ...actor, role_id: "TERAPEUTA" }]) denied(() => authorize("admin-delete-user", bodies["admin-delete-user"], invalid, target, center));
});
test("contracts accept KEEP SET CLEAR and reject ambiguous and injected fields", () => {
  for (const [op, body] of Object.entries(bodies)) assert.doesNotThrow(() => validate(op, body));
  for (const entityId of [null, { action: "CLEAR" }, { action: "SET", value: centerId }]) assert.doesNotThrow(() => validate("admin-update-user", { targetUserId: targetId, entityId }));
  for (const body of [{ targetUserId: targetId, role_id: "SUPER_ADMIN" }, { targetUserId: targetId, entityId: { action: "SET" } }, { targetUserId: targetId, entityId: { action: "CLEAR", value: centerId } }, { targetUserId: targetId, phone: "123", clearPhone: true }, { targetUserId: targetId, status: "ACTIVE" }]) assert.throws(() => validate("admin-update-user", body));
  assert.throws(() => validate("admin-set-entity-status", { entityId: centerId, status: "active" }));
  assert.throws(() => validate("admin-set-entity-license", { entityId: centerId }));
});

function setup(operation, { profile = actor, identityStatus = 200, broken = false } = {}) {
  const calls = [];
  const handler = createHandler(operation, { url: "https://backend.invalid", serverKey: "synthetic-key", allowedOrigins: ["https://app.invalid"],
    fetchImpl: async (url, options) => {
      calls.push({ url, options });
      assert.equal(options.method, "GET");
      if (broken) throw new Error("synthetic confidential upstream error");
      if (url.endsWith("/auth/v1/user")) { assert.equal(options.headers.Authorization, "Bearer synthetic-token"); return Response.json({ id: actorId }, { status: identityStatus }); }
      return Response.json(url.includes("/entities?") ? [center] : url.includes(actorId) ? [profile] : [target]);
    } });
  return { handler, calls };
}
const request = (body, extra = {}) => new Request("https://function.invalid", { method: "POST", headers: { Authorization: "Bearer synthetic-token", "Content-Type": "application/json", ...extra }, body: JSON.stringify(body) });
test("all seven authorized endpoints return explicit unavailable with zero writes", async () => {
  for (const [operation, body] of Object.entries(bodies)) {
    const { handler, calls } = setup(operation, { profile: { ...actor, role_id: "SUPER_ADMIN" } });
    const result = await handler(request(body));
    assert.equal(result.status, 503);
    assert.deepEqual((await result.json()).code, "ATOMIC_BACKEND_REQUIRED");
    assert.ok(calls.length >= 2);
    assert.ok(calls.every(c => c.options.method === "GET"));
  }
});
test("identity failures, unavailable reads and permission failures never succeed", async () => {
  for (const options of [{ identityStatus: 401 }, { broken: true }, { profile: { ...actor, role_id: "AUXILIAR" } }]) {
    const { handler } = setup("admin-update-user", options);
    const result = await handler(request(bodies["admin-update-user"]));
    assert.ok(result.status >= 400);
    const body = await result.text(); assert.equal(body.includes("confidential"), false); assert.equal(body.includes("synthetic-key"), false);
  }
});
test("missing tokens, foreign origins, malformed and oversized requests never access backend", async () => {
  for (const req of [request(bodies["admin-delete-user"], { Authorization: "" }), request(bodies["admin-delete-user"], { Origin: "https://foreign.invalid" }), request({ targetUserId: "bad" }), request({ data: "x".repeat(17000) })]) {
    const { handler, calls } = setup("admin-delete-user");
    assert.ok((await handler(req)).status >= 400); assert.equal(calls.length, 0);
  }
});
test("preflight and absent server configuration make no backend requests", async () => {
  const { handler, calls } = setup("admin-delete-user");
  assert.equal((await handler(new Request("https://function.invalid", { method: "OPTIONS" }))).status, 204);
  assert.equal(calls.length, 0);
  assert.equal((await createHandler("admin-delete-user")(request(bodies["admin-delete-user"]))).status, 503);
});
