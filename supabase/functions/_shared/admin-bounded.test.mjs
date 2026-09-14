import test from 'node:test';
import assert from 'node:assert/strict';
import { createBoundedHandler, validateBounded } from './admin-bounded.mjs';
import { authorize } from './admin-phase2.mjs';

const actor = '11111111-1111-4111-8111-111111111111';
const target = '22222222-2222-4222-8222-222222222222';
const key = '33333333-3333-4333-8333-333333333333';
const center = '44444444-4444-4444-8444-444444444444';
const body = { targetUserId: target, fullName: 'Synthetic Name' };
const request = (patch = body, headers = {}) => new Request('https://local.invalid', {
  method: 'POST', headers: { authorization: 'Bearer synthetic-token',
    'content-type': 'application/json', 'idempotency-key': key, ...headers },
  body: JSON.stringify(patch),
});

test('validation rejects injected privileges, invalid clear, empty name and ambiguous dates', () => {
  for (const patch of [{ ...body, actorId: actor }, { ...body, fullName: ' ' },
    { ...body, phone: 'x', clearPhone: true }, { ...body, entityId: { action: 'SET' } }]) {
    assert.throws(() => validateBounded('admin-update-user', patch));
  }
  assert.throws(() => validateBounded('admin-create-entity', { name: 'X', cif: 'X', licenseExpiresAt: '2030-01-01' }));
  for (const operation of ['admin-create-user', 'admin-delete-user', 'admin-delete-entity', 'admin-set-entity-status']) {
    assert.throws(() => createBoundedHandler(operation));
  }
});

test('existing authorization precheck rejects cross-center, admin targets, self and inactive actor', () => {
  // Tests existing JS precheck, NOT SQL authorization, locks or grants.
  const profile = { id: actor, role_id: 'ADMIN_CENTRO', entity_id: center, is_active: true };
  const staff = { id: target, role_id: 'TERAPEUTA', entity_id: center };
  const entity = { id: center, status: 'ACTIVE', license_expires_at: null };
  assert.doesNotThrow(() => authorize('admin-update-user', body, profile, staff, entity));
  for (const destination of [{ ...staff, entity_id: key }, { ...staff, role_id: 'ADMIN_CENTRO' }]) {
    assert.throws(() => authorize('admin-update-user', body, profile, destination, entity));
  }
  assert.throws(() => authorize('admin-update-user', { targetUserId: actor }, profile, profile, entity));
  assert.throws(() => authorize('admin-update-user', body, { ...profile, is_active: false }, staff, entity));
  assert.throws(() => authorize('admin-create-entity', { name: 'X', cif: 'X' }, profile, null, entity));
});

test('missing transport fails closed and invalid auth never reaches RPC', async () => {
  assert.equal((await createBoundedHandler('admin-update-user')(request())).status, 503);
  let calls = 0;
  const handler = createBoundedHandler('admin-update-user', {
    authenticate: async () => { throw new Error('private token details'); }, rpc: async () => { calls++; },
  });
  assert.equal((await handler(request())).status, 401);
  assert.equal(calls, 0);
});

test('verified identity and exact request reach one RPC; only safe result escapes', async () => {
  const calls = [];
  const handler = createBoundedHandler('admin-update-user', {
    authenticate: async token => { assert.equal(token, 'Bearer synthetic-token'); return { id: actor }; },
    rpc: async (...args) => { calls.push(args); return { success: true, code: 'COMPLETED', secret: 'synthetic-private' }; },
  });
  assert.deepEqual(await (await handler(request())).json(), { success: true, code: 'COMPLETED' });
  assert.equal(calls.length, 1);
  assert.equal(calls[0][0], 'admin_phase2_execute');
  assert.equal(calls[0][1].p_actor_id, actor);
  assert.deepEqual(calls[0][1].p_body, body);
});

test('synthetic database rejection reports whole operation failed, without upstream details', async () => {
  const handler = createBoundedHandler('admin-update-user', {
    authenticate: async () => ({ id: actor }),
    rpc: async () => ({ success: false, code: 'EMAIL_CHANGE_UNAVAILABLE' }),
  });
  const response = await handler(request({ ...body, email: 'other@example.invalid' }));
  assert.equal(response.status, 409);
  assert.deepEqual(await response.json(), { success: false, code: 'EMAIL_CHANGE_UNAVAILABLE' });
  // This is response handling, not proof that PostgreSQL rolled back the patch.
});

test('transport timeout never retries and explicit result/close dispatch separately', async () => {
  const calls = [];
  const handler = createBoundedHandler('admin-update-user', {
    timeoutMs: 15, authenticate: async () => ({ id: actor }), rpc: async name => {
      calls.push(name);
      if (name.endsWith('_execute')) return new Promise(() => {});
      return { success: false, code: name.endsWith('_result') ? 'NOT_OBSERVED' : 'CANCELLED' };
    },
  });
  assert.equal((await (await handler(request())).json()).code, 'OUTCOME_UNKNOWN');
  assert.deepEqual(calls, ['admin_phase2_execute']);
  assert.equal((await (await handler(request(body, { 'x-admin-mode': 'result' }))).json()).code, 'NOT_OBSERVED');
  assert.equal((await (await handler(request(body, { 'x-admin-mode': 'close' }))).json()).code, 'CANCELLED');
  assert.deepEqual(calls, ['admin_phase2_execute', 'admin_phase2_result', 'admin_phase2_close']);
});

test('invalid success response becomes uncertain, not successful or automatically retried', async () => {
  let calls = 0;
  const handler = createBoundedHandler('admin-create-entity', {
    authenticate: async () => ({ id: actor }), rpc: async () => { calls++; return { success: true, code: 'COMPLETED' }; },
  });
  assert.equal((await (await handler(request({ name: 'X', cif: 'X' }))).json()).code, 'OUTCOME_UNKNOWN');
  assert.equal(calls, 1);
});

test('CORS permits idempotency headers only for allowed origins; preflight performs no IO', async () => {
  let calls = 0;
  const handler = createBoundedHandler('admin-update-user', {
    allowedOrigins: ['https://app.invalid'], authenticate: async () => { calls++; return { id: actor }; }, rpc: async () => { calls++; },
  });
  assert.equal((await handler(request(body, { origin: 'https://foreign.invalid' }))).status, 403);
  const response = await handler(new Request('https://local.invalid', { method: 'OPTIONS', headers: { origin: 'https://app.invalid' } }));
  assert.equal(response.status, 204);
  assert.match(response.headers.get('access-control-allow-headers'), /idempotency-key/);
  assert.equal(calls, 0);
});

test('known SQL rejection is definitive; an arbitrary upstream message stays uncertain', async () => {
  for (const [sqlState, expectedStatus, code] of [['P0001', 403, 'FORBIDDEN'], [undefined, 202, 'OUTCOME_UNKNOWN']]) {
    const handler = createBoundedHandler('admin-update-user', {
      authenticate: async () => ({ id: actor }),
      rpc: async () => { throw Object.assign(new Error('FORBIDDEN'), { sqlState }); },
    });
    const response = await handler(request());
    assert.equal(response.status, expectedStatus);
    assert.equal((await response.json()).code, code);
  }
});

test('result retrieves synthetic committed response after original response is lost', async () => {
  let writes = 0;
  let receipt;
  const handler = createBoundedHandler('admin-create-entity', {
    authenticate: async () => ({ id: actor }),
    rpc: async name => {
      if (name.endsWith('_execute')) {
        writes++;
        receipt = { success: true, code: 'COMPLETED', data: { entityId: center } };
        throw new Error('synthetic response lost after commit');
      }
      return receipt;
    },
  });
  const patch = { name: 'Synthetic', cif: 'Synthetic' };
  assert.equal((await (await handler(request(patch))).json()).code, 'OUTCOME_UNKNOWN');
  assert.deepEqual(await (await handler(request(patch, { 'x-admin-mode': 'result' }))).json(), receipt);
  assert.equal(writes, 1);
  // Simulates a receipt; proves adapter recovery only, not database commit behavior.
});

test('oversized and malformed bodies fail without identity or RPC access', async () => {
  let calls = 0;
  const handler = createBoundedHandler('admin-update-user', {
    authenticate: async () => { calls++; return { id: actor }; }, rpc: async () => { calls++; },
  });
  assert.equal((await handler(request({ ...body, fullName: 'x'.repeat(17000) }))).status, 400);
  assert.equal((await handler(request(body, { 'idempotency-key': 'bad' }))).status, 400);
  assert.equal(calls, 0);
});
