import test from 'node:test';
import assert from 'node:assert/strict';
import { createUserHandler } from './admin-create-user.mjs';
const body = { email: 'synthetic@example.invalid', password: 'synthetic-password', fullName: 'Synthetic',
  roleId: 'TERAPEUTA', entityId: '11111111-1111-1111-1111-111111111111', isActive: true };
const request = (mode = 'execute', value = body) => new Request('https://test.invalid', { method: 'POST',
  headers: { authorization: 'Bearer synthetic', 'content-type': 'application/json',
    'idempotency-key': '22222222-2222-2222-2222-222222222222', 'x-admin-mode': mode }, body: JSON.stringify(value) });
const setup = replies => {
  const calls = [];
  return { calls, handler: createUserHandler({ url: 'https://test.invalid', serverKey: 'synthetic-server-key',
    fetchImpl: async (url, options) => { calls.push({ url, options }); const reply = replies.shift();
      if (reply instanceof Error) throw reply; return Response.json(reply?.body ?? reply, { status: reply?.status ?? 200 }); } }) };
};
test('verified actor, one Auth call and no password in RPCs or result', async () => {
  const { handler, calls } = setup([{ id: 'verified-actor' }, { code: 'PREPARED', ticket: 'ticket' }, {},
    { success: true, code: 'COMPLETED', data: { userId: 'created-id' } }]);
  const response = await handler(request());
  assert.equal(response.status, 200);
  assert.equal(calls.filter(c => c.url.endsWith('/admin/users')).length, 1);
  for (const call of calls.filter(c => c.url.includes('/rpc/'))) {
    assert.equal(JSON.parse(call.options.body).p_actor_id, 'verified-actor');
    assert.equal(call.options.body.includes(body.password), false);
  }
  assert.equal((await response.text()).includes(body.password), false);
});
test('authorization rejection happens before Auth mutation', async () => {
  const { handler, calls } = setup([{ id: 'actor' }, { status: 400, body: { code: 'P0001', message: 'FORBIDDEN' } }]);
  assert.equal((await handler(request())).status, 403);
  assert.equal(calls.length, 2);
});
test('uncertain Auth result is not retried or represented as success', async () => {
  const { handler, calls } = setup([{ id: 'actor' }, { code: 'PREPARED', ticket: 'ticket' }, new Error('timeout')]);
  const response = await handler(request());
  assert.equal(response.status, 202);
  assert.equal((await response.json()).success, false);
  assert.equal(calls.length, 3);
});
test('identity creation receives the longer trigger-aware timeout', async () => {
  const { handler, calls } = setup([{ id: 'actor' }, { code: 'PREPARED', ticket: 'ticket' }, {},
    { success: true, code: 'COMPLETED', data: { userId: 'id' } }]);
  await handler(request());
  const authCreate = calls.find(call => call.url.endsWith('/auth/v1/admin/users'));
  assert.equal(authCreate.options.signal.aborted, false);
});
test('recovery requires no password and never invokes Auth creation', async () => {
  const { password, ...safe } = body;
  const { handler, calls } = setup([{ id: 'actor' }, { success: true, code: 'COMPLETED', data: { userId: 'id' } }]);
  assert.equal((await handler(request('result', safe))).status, 200);
  assert.equal(calls.length, 2);
});
test('client actor and unknown permission fields are rejected', async () => {
  const { handler, calls } = setup([]);
  assert.equal((await handler(request('execute', { ...body, actorId: 'spoofed' }))).status, 400);
  assert.equal(calls.length, 0);
});
