import test from 'node:test';
import assert from 'node:assert/strict';
import { createDeployedHandler } from './admin-bounded-transport.mjs';

const actor = '11111111-1111-4111-8111-111111111111';
const target = '22222222-2222-4222-8222-222222222222';
const request = () => new Request('https://function.invalid', { method: 'POST',
  headers: { authorization: 'Bearer synthetic-user-token', 'content-type': 'application/json',
    'idempotency-key': '33333333-3333-4333-8333-333333333333' },
  body: JSON.stringify({ targetUserId: target, fullName: 'Synthetic' }),
});
test('deployed transport verifies user JWT before server RPC and preserves actor identity', async () => {
  const calls = [];
  const handler = createDeployedHandler('admin-update-user', { url: 'https://backend.invalid', serverKey: 'synthetic-server-key',
    fetchImpl: async (url, options) => {
      calls.push(url);
      assert.equal(options.redirect, 'error');
      if (url.endsWith('/auth/v1/user')) {
        assert.equal(options.headers.Authorization, 'Bearer synthetic-user-token');
        return Response.json({ id: actor, role: 'ignored' });
      }
      assert.equal(url, 'https://backend.invalid/rest/v1/rpc/admin_phase2_execute');
      assert.equal(options.headers.Authorization, 'Bearer synthetic-server-key');
      assert.equal(JSON.parse(options.body).p_actor_id, actor);
      return Response.json({ success: true, code: 'COMPLETED' });
    } });
  assert.equal((await handler(request())).status, 200);
  assert.equal(calls.length, 2);
});
test('invalid identity never invokes the write RPC', async () => {
  let calls = 0;
  const handler = createDeployedHandler('admin-update-user', { url: 'https://backend.invalid', serverKey: 'synthetic',
    fetchImpl: async () => { calls++; return Response.json({}, { status: 401 }); } });
  assert.equal((await handler(request())).status, 401);
  assert.equal(calls, 1);
});
test('only recognized database errors escape; all other failures stay uncertain', async () => {
  for (const known of [true, false]) {
    const handler = createDeployedHandler('admin-update-user', { url: 'https://backend.invalid', serverKey: 'synthetic',
      fetchImpl: async url => url.endsWith('/auth/v1/user') ? Response.json({ id: actor }) :
        Response.json({ code: known ? 'P0001' : 'XXXXX', message: known ? 'FORBIDDEN' : 'confidential detail' }, { status: 400 }) });
    const result = await handler(request());
    assert.equal(result.status, known ? 403 : 202);
    assert.equal((await result.text()).includes('confidential'), false);
  }
});
