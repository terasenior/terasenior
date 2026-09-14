import { createBoundedHandler } from './admin-bounded.mjs';

// Server-only transport. No credentials are returned, logged or accepted in a body.
export function createDeployedHandler(operation, { url, serverKey, allowedOrigins = [], fetchImpl = fetch } = {}) {
  if (!url || !serverKey || !url.startsWith('https://')) return createBoundedHandler(operation);
  const base = url.replace(/\/$/, '');
  const send = (path, options) => fetchImpl(`${base}${path}`, {
    ...options, redirect: 'error', signal: AbortSignal.timeout(9000),
  });
  return createBoundedHandler(operation, {
    allowedOrigins,
    authenticate: async authorization => {
      const response = await send('/auth/v1/user', {
        method: 'GET', headers: { apikey: serverKey, Authorization: authorization },
      });
      if (!response.ok) throw new Error('UNAUTHENTICATED');
      const identity = await response.json();
      return { id: identity?.id };
    },
    rpc: async (name, body) => {
      if (!['admin_phase2_execute', 'admin_phase2_result', 'admin_phase2_close'].includes(name)) throw new Error('INVALID_RPC');
      const response = await send(`/rest/v1/rpc/${name}`, {
        method: 'POST',
        headers: { apikey: serverKey, Authorization: `Bearer ${serverKey}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!response.ok) {
        const failure = await response.json().catch(() => null);
        const known = ['FORBIDDEN', 'INVALID_INPUT', 'OPERATION_BLOCKED', 'IDEMPOTENCY_CONFLICT'];
        if (failure?.code === 'P0001' && known.includes(failure.message)) {
          throw Object.assign(new Error(failure.message), { sqlState: 'P0001' });
        }
        throw new Error('RPC_UNCONFIRMED');
      }
      return response.json();
    },
  });
}
