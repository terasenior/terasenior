import { validate } from './admin-phase2.mjs';
import { jsonBody } from './admin-bounded.mjs';

// Auth INSERT, profile, receipt and audit commit together through the ticket trigger.
export function createUserHandler({ url, serverKey, allowedOrigins = [], fetchImpl = fetch }) {
  return async request => {
    const origin = request.headers.get('origin');
    const headers = { 'Cache-Control': 'no-store', Vary: 'Origin' };
    if (allowedOrigins.includes(origin)) Object.assign(headers, {
      'Access-Control-Allow-Origin': origin, 'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'authorization, apikey, content-type, x-client-info, idempotency-key, x-admin-mode',
    });
    const respond = (status, result) => Response.json(result, { status, headers });
    const fail = (status, code) => respond(status, { success: false, code });
    if (origin && !allowedOrigins.includes(origin)) return fail(403, 'FORBIDDEN_ORIGIN');
    if (request.method === 'OPTIONS') return new Response(null, { status: 204, headers });
    if (request.method !== 'POST') return fail(405, 'METHOD_NOT_ALLOWED');
    const authorization = request.headers.get('authorization') ?? '';
    if (!/^Bearer \S+$/i.test(authorization)) return fail(401, 'UNAUTHENTICATED');
    const key = request.headers.get('idempotency-key') ?? '';
    const mode = request.headers.get('x-admin-mode') ?? 'execute';
    if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(key) ||
      !['execute', 'result', 'close'].includes(mode) ||
      request.headers.get('content-type')?.split(';')[0].trim() !== 'application/json') return fail(400, 'INVALID_INPUT');
    let body;
    try {
      body = await jsonBody(request, 10000);
      validate('admin-create-user', mode === 'execute' ? body : { ...body, password: 'unused-recovery' });
      if (mode !== 'execute' && 'password' in body) return fail(400, 'INVALID_INPUT');
    } catch { return fail(400, 'INVALID_INPUT'); }
    if (!url?.startsWith('https://') || !serverKey) return fail(503, 'UNAVAILABLE');
    const send = (path, options) => fetchImpl(`${url.replace(/\/$/, '')}${path}`, {
      ...options, redirect: 'error', signal: AbortSignal.timeout(9000),
    });
    const serverHeaders = { apikey: serverKey, Authorization: `Bearer ${serverKey}`, 'Content-Type': 'application/json' };
    let identity;
    try {
      const auth = await send('/auth/v1/user', { headers: { apikey: serverKey, Authorization: authorization } });
      if (!auth.ok) return fail(401, 'UNAUTHENTICATED');
      identity = await auth.json();
      if (!identity?.id) return fail(401, 'UNAUTHENTICATED');
    } catch { return fail(401, 'UNAUTHENTICATED'); }
    const { password, ...safeBody } = body;
    const control = async controlMode => {
      const response = await send('/rest/v1/rpc/admin_create_user_control', {
        method: 'POST', headers: serverHeaders,
        body: JSON.stringify({ p_actor_id: identity.id, p_request_id: key, p_body: safeBody, p_mode: controlMode }),
      });
      const result = await response.json();
      if (!response.ok) {
        if (result.code === 'P0001' && ['FORBIDDEN', 'INVALID_INPUT', 'INVALID_ASSOCIATION', 'IDEMPOTENCY_CONFLICT'].includes(result.message)) {
          throw Object.assign(new Error(result.message), { known: true });
        }
        throw new Error('OUTCOME_UNKNOWN');
      }
      return result;
    };
    try {
      let result = await control(mode === 'execute' ? 'prepare' : mode);
      if (result.code === 'PREPARED') {
        if (mode !== 'execute' || !result.ticket) throw new Error('INVALID_RESULT');
        const created = await send('/auth/v1/admin/users', {
          method: 'POST', headers: serverHeaders,
          body: JSON.stringify({ email: safeBody.email, password, email_confirm: true,
            app_metadata: { admin_creation_ticket: result.ticket }, user_metadata: { full_name: safeBody.fullName } }),
        });
        result = await control('result');
        if (!created.ok && result.code !== 'COMPLETED') {
          const closed = await control('close');
          result = closed.code === 'CANCELLED' ? { success: false, code: 'CREATION_REJECTED' } : closed;
        }
      }
      if (result.success === true && result.code === 'COMPLETED' && result.data?.userId) {
        return respond(200, { success: true, code: 'COMPLETED', data: { userId: result.data.userId } });
      }
      if (['CANCELLED', 'CREATION_REJECTED'].includes(result.code)) return fail(409, result.code);
      return fail(202, 'NOT_OBSERVED');
    } catch (error) {
      return error.known ? fail(error.message === 'FORBIDDEN' ? 403 : 400, error.message) : fail(202, 'OUTCOME_UNKNOWN');
    }
  };
}
