// Local adapter preparation. No default network transport, secrets or Deno entry.
// A future server entry must supply verified Auth identity and server-only RPC.
import { validate } from './admin-phase2.mjs';

const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const operations = new Set(['admin-create-entity', 'admin-update-user']);
const codes = new Set(['COMPLETED', 'CANCELLED', 'IN_PROGRESS', 'NOT_OBSERVED',
  'EMAIL_CHANGE_UNAVAILABLE', 'PROTECTED_ROLE', 'FORBIDDEN', 'INVALID_ASSOCIATION',
  'ENTITY_TRANSFER_UNAVAILABLE', 'DATABASE_FAILURE', 'CONSTRAINT_REJECTED']);

export function validateBounded(operation, body) {
  if (!operations.has(operation)) throw new Error('OPERATION_BLOCKED');
  validate(operation, body);
  if (body.fullName != null && !body.fullName.trim()) throw new Error('INVALID_INPUT');
  if (body.licenseExpiresAt != null &&
    !/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,6})?(Z|[+-]\d{2}:\d{2})$/.test(body.licenseExpiresAt)) {
    throw new Error('INVALID_INPUT');
  }
  // Role, current email and associations are checked again in SQL under locks.
  return body;
}

async function jsonBody(request, timeoutMs) {
  const reader = request.body?.getReader();
  if (!reader) throw new Error('INVALID_INPUT');
  let size = 0;
  const chunks = [];
  const endsAt = Date.now() + timeoutMs;
  try {
    for (;;) {
      const remaining = endsAt - Date.now();
      if (remaining <= 0) throw new Error('INVALID_INPUT');
      const { done, value } = await deadline(() => reader.read(), remaining);
      if (done) break;
      size += value.byteLength;
      if (size > 16384) throw new Error('INVALID_INPUT');
      chunks.push(value);
    }
  } finally { await reader.cancel().catch(() => {}); }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
  return JSON.parse(new TextDecoder('utf-8', { fatal: true }).decode(bytes));
}

async function deadline(action, ms) {
  let timer;
  try {
    return await Promise.race([Promise.resolve().then(action), new Promise((_, reject) => {
      timer = setTimeout(() => reject(new Error('TIMEOUT')), ms);
    })]);
  } finally { clearTimeout(timer); }
}

export function createBoundedHandler(operation, {
  authenticate, rpc, allowedOrigins = [], timeoutMs = 10000,
} = {}) {
  if (!operations.has(operation)) throw new Error('OPERATION_BLOCKED');
  return async request => {
    const origin = request.headers.get('origin');
    const headers = { 'Content-Type': 'application/json', 'Cache-Control': 'no-store', Vary: 'Origin' };
    if (origin && allowedOrigins.includes(origin)) Object.assign(headers, {
      'Access-Control-Allow-Origin': origin,
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'authorization, apikey, content-type, x-client-info, idempotency-key, x-admin-mode',
    });
    const respond = (status, body) => Response.json(body, { status, headers });
    const fail = (status, code) => respond(status, { success: false, code });
    if (origin && !allowedOrigins.includes(origin)) return fail(403, 'FORBIDDEN_ORIGIN');
    if (request.method === 'OPTIONS') return new Response(null, { status: 204, headers });
    if (request.method !== 'POST') return fail(405, 'METHOD_NOT_ALLOWED');
    if (typeof authenticate !== 'function' || typeof rpc !== 'function') return fail(503, 'UNAVAILABLE');
    const token = request.headers.get('authorization') ?? '';
    if (!/^Bearer \S+$/i.test(token)) return fail(401, 'UNAUTHENTICATED');
    const key = request.headers.get('idempotency-key') ?? '';
    const mode = request.headers.get('x-admin-mode') ?? 'execute';
    if (!uuid.test(key) || !['execute', 'result', 'close'].includes(mode) ||
      request.headers.get('content-type')?.split(';')[0].trim() !== 'application/json') return fail(400, 'INVALID_INPUT');
    let body;
    try { body = validateBounded(operation, await jsonBody(request, timeoutMs)); }
    catch { return fail(400, 'INVALID_INPUT'); }
    let identity;
    try { identity = await deadline(() => authenticate(token), timeoutMs); }
    catch { return fail(401, 'UNAUTHENTICATED'); }
    if (!uuid.test(identity?.id ?? '')) return fail(401, 'UNAUTHENTICATED');
    // The body never supplies actor identity. No automatic retry, including close.
    try {
      const result = await deadline(() => rpc(`admin_phase2_${mode}`, {
        p_actor_id: identity.id.toLowerCase(), p_request_id: key.toLowerCase(),
        p_operation: operation, p_body: body,
      }), timeoutMs);
      if (!result || typeof result.success !== 'boolean' || !codes.has(result.code) ||
        result.success !== (result.code === 'COMPLETED')) throw new Error('INVALID_RESULT');
      const safe = { success: result.success, code: result.code };
      if (result.success && operation === 'admin-create-entity') {
        if (!uuid.test(result.data?.entityId ?? '')) throw new Error('INVALID_RESULT');
        safe.data = { entityId: result.data.entityId };
      }
      const pending = ['IN_PROGRESS', 'NOT_OBSERVED'].includes(result.code);
      return respond(pending ? 202 : result.success ? 200 : 409, safe);
    } catch (error) {
      // Trusted transport must preserve PostgreSQL SQLSTATE separately from text.
      // These exceptions occur outside the mutation subtransaction and abort RPC.
      if (error?.sqlState === 'P0001' && ['FORBIDDEN', 'INVALID_INPUT',
        'OPERATION_BLOCKED', 'IDEMPOTENCY_CONFLICT'].includes(error.message)) {
        return fail(error.message === 'FORBIDDEN' ? 403 : error.message === 'INVALID_INPUT' ? 400 : 409, error.message);
      }
      // A rejected transport may follow a committed write. Never claim rollback.
      return fail(202, 'OUTCOME_UNKNOWN');
    }
  };
}
