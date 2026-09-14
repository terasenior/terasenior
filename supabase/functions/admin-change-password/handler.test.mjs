import test from "node:test";
import assert from "node:assert/strict";
import { canChangePassword, createHandler } from "./handler.mjs";

const callerId = "11111111-1111-4111-8111-111111111111";
const targetId = "22222222-2222-4222-8222-222222222222";
const caller = { id: callerId, role_id: "ADMIN_CENTRO", entity_id: "center-a", is_active: true };
const target = { id: targetId, role_id: "TERAPEUTA", entity_id: "center-a", is_active: true };
const entity = { id: "center-a", status: "ACTIVE", license_expires_at: null };

test("authorization matrix denies cross-center, peers, privileged targets and self", () => {
  assert.equal(canChangePassword(caller, target, entity), true);
  assert.equal(canChangePassword(caller, { ...target, role_id: "AUXILIAR" }, entity), true);
  for (const role of ["SUPER_ADMIN", "ADMIN_CENTRO", "UNKNOWN"]) {
    assert.equal(canChangePassword(caller, { ...target, role_id: role }, entity), false);
  }
  assert.equal(canChangePassword(caller, { ...target, entity_id: "center-b" }, entity), false);
  assert.equal(canChangePassword(caller, caller, entity), false);
  assert.equal(canChangePassword({ ...caller, is_active: false }, target, entity), false);
  assert.equal(canChangePassword({ ...caller, role_id: "TERAPEUTA" }, target, entity), false);
  assert.equal(canChangePassword(caller, target, { ...entity, status: "INACTIVE" }), false);
  for (const expiry of ["invalid", "2000-01-01T00:00:00Z", undefined]) {
    assert.equal(canChangePassword(caller, target, { ...entity, license_expires_at: expiry }), false);
  }
  assert.equal(canChangePassword(caller, target, null), false);
  assert.equal(canChangePassword(caller, null, entity), false);
  assert.equal(canChangePassword({ ...caller, role_id: "SUPER_ADMIN" }, { ...target, entity_id: "other" }, null), true);
});

// Synthetic placeholders only. All network calls are replaced by this fake.
function setup({ profile = caller, destination = target, center = entity, identityStatus = 200,
  writeStatus = 200, lookupFailure = false } = {}) {
  const calls = [];
  const handler = createHandler({
    url: "https://supabase.invalid", serverKey: "synthetic-test-placeholder",
    allowedOrigins: ["https://app.invalid"],
    fetchImpl: async (url, options) => {
      calls.push({ url, ...options });
      if (url.endsWith("/auth/v1/user")) return Response.json({ id: callerId }, { status: identityStatus });
      if (url.includes("/rest/v1/")) {
        if (lookupFailure) throw new Error("synthetic sensitive upstream error");
        const row = url.includes("/entities?") ? center : url.includes(callerId) ? profile : destination;
        return Response.json(row ? [row] : []);
      }
      assert.equal(url, `https://supabase.invalid/auth/v1/admin/users/${targetId}`);
      assert.equal(options.method, "PUT");
      assert.deepEqual(JSON.parse(options.body), { password: "synthetic-password" });
      return Response.json({}, { status: writeStatus });
    },
  });
  return { handler, calls };
}
function request(body = { targetUserId: targetId, newPassword: "synthetic-password" }, headers = {}) {
  return new Request("https://function.invalid", { method: "POST",
    headers: { Authorization: "Bearer synthetic-user-token", "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
  });
}

test("writes only selected account after identity and permission checks", async () => {
  const { handler, calls } = setup();
  assert.equal((await handler(request())).status, 204);
  assert.equal(calls.filter((call) => call.method === "PUT").length, 1);
  assert.equal(calls[0].headers.Authorization, "Bearer synthetic-user-token");
  assert.notEqual(calls.at(-1).headers.Authorization, calls[0].headers.Authorization);
});

test("unauthorized and unavailable lookups never perform a write", async () => {
  for (const options of [
    { identityStatus: 401 }, { profile: { ...caller, is_active: false } },
    { profile: { ...caller, role_id: "AUXILIAR" } }, { destination: null },
    { destination: { ...target, entity_id: "other" } },
    { destination: { ...target, role_id: "SUPER_ADMIN" } },
    { center: { ...entity, status: "INACTIVE" } }, { lookupFailure: true },
  ]) {
    const { handler, calls } = setup(options);
    const response = await handler(request());
    assert.ok(response.status >= 400);
    assert.equal(await response.text(), "");
    assert.equal(calls.some((call) => call.method === "PUT"), false);
  }
});

test("rejects forged attributes, missing tokens, invalid input and foreign origins", async () => {
  for (const req of [
    request({ targetUserId: targetId, newPassword: "synthetic-password", role: "SUPER_ADMIN" }),
    request({ targetUserId: "bad-id", newPassword: "synthetic-password" }),
    request({ targetUserId: targetId, newPassword: "short" }),
    request(undefined, { Authorization: "" }),
    request(undefined, { Origin: "https://attacker.invalid" }),
    request({ targetUserId: targetId, newPassword: "x".repeat(9000) }),
  ]) {
    const { handler, calls } = setup();
    assert.ok((await handler(req)).status >= 400);
    assert.equal(calls.length, 0);
  }
});

test("self changes are denied before any administrative lookup or write", async () => {
  const { handler, calls } = setup();
  assert.equal((await handler(request({ targetUserId: callerId, newPassword: "synthetic-password" }))).status, 403);
  assert.equal(calls.length, 1);
});

test("Auth failures are reported without exposing upstream data", async () => {
  for (const status of [422, 429, 500]) {
    const { handler } = setup({ writeStatus: status });
    const response = await handler(request());
    assert.equal(response.status, status === 500 ? 502 : status);
    assert.equal(await response.text(), "");
  }
});

test("preflight does not call backend; configuration fails closed", async () => {
  const { handler, calls } = setup();
  const response = await handler(new Request("https://function.invalid", {
    method: "OPTIONS", headers: { Origin: "https://app.invalid" },
  }));
  assert.equal(response.status, 204);
  assert.equal(response.headers.get("Access-Control-Allow-Origin"), "https://app.invalid");
  assert.equal(calls.length, 0);
  const unavailable = createHandler({ fetchImpl: () => { throw new Error("Must not call network"); } });
  assert.equal((await unavailable(request())).status, 503);
});
