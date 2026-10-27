const { test, describe, before, after, beforeEach } = require('node:test');
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');
const fs = require('node:fs');

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'demo-no-project',
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

describe('spa_workspaces security rules', () => {
  test('denies unauthenticated read and write to spa_workspaces', async () => {
    const anonDb = testEnv.unauthenticatedContext().firestore();
    await assertFails(anonDb.doc('spa_workspaces/khiem_thi_viet_shared').get());
    await assertFails(
      anonDb.doc('spa_workspaces/khiem_thi_viet_shared').set({
        workspaceId: 'khiem_thi_viet_shared',
        shopName: 'KHIẾM THỊ VIỆT',
        payloadJson: '{"taiKhoans":[]}',
        accountsCount: 1,
        customersCount: 0,
        ticketsCount: 0,
        updatedByUid: 'anon',
        updatedByName: 'Admin',
        updatedAt: new Date(),
      })
    );
  });

  test('allows authenticated user to create and update valid spa_workspaces document and read it from second device', async () => {
    const adminDb = testEnv.authenticatedContext('admin_uid_1').firestore();
    const staffDb = testEnv.authenticatedContext('staff_uid_2').firestore();

    await assertSucceeds(
      adminDb.doc('spa_workspaces/khiem_thi_viet_shared').set({
        workspaceId: 'khiem_thi_viet_shared',
        shopName: 'KHIẾM THỊ VIỆT',
        payloadJson: '{"taiKhoans":[{"id":"tk_admin"}]}',
        accountsCount: 2,
        customersCount: 3,
        ticketsCount: 4,
        updatedByUid: 'admin_uid_1',
        updatedByName: 'Admin Quản trị',
        updatedAt: new Date(),
      })
    );

    await assertSucceeds(staffDb.doc('spa_workspaces/khiem_thi_viet_shared').get());

    await assertSucceeds(
      staffDb.doc('spa_workspaces/khiem_thi_viet_shared').update({
        payloadJson: '{"taiKhoans":[{"id":"tk_admin"},{"id":"tk_nv1"}]}',
        ticketsCount: 5,
        updatedByUid: 'staff_uid_2',
        updatedByName: 'Nguyễn Văn Minh',
        updatedAt: new Date(),
      })
    );
  });

  test('denies spoofing updatedByUid, extra undeclared fields, or deleting spa_workspaces', async () => {
    const adminDb = testEnv.authenticatedContext('admin_uid_1').firestore();
    await assertFails(
      adminDb.doc('spa_workspaces/khiem_thi_viet_shared').set({
        workspaceId: 'khiem_thi_viet_shared',
        shopName: 'KHIẾM THỊ VIỆT',
        payloadJson: '{"taiKhoans":[]}',
        accountsCount: 1,
        customersCount: 0,
        ticketsCount: 0,
        updatedByUid: 'someone_else_uid',
        updatedByName: 'Admin Quản trị',
        updatedAt: new Date(),
      })
    );

    await assertFails(
      adminDb.doc('spa_workspaces/khiem_thi_viet_shared').set({
        workspaceId: 'khiem_thi_viet_shared',
        shopName: 'KHIẾM THỊ VIỆT',
        payloadJson: '{"taiKhoans":[]}',
        accountsCount: 1,
        customersCount: 0,
        ticketsCount: 0,
        updatedByUid: 'admin_uid_1',
        updatedByName: 'Admin Quản trị',
        extraField: 'not_allowed',
        updatedAt: new Date(),
      })
    );

    await assertSucceeds(
      adminDb.doc('spa_workspaces/khiem_thi_viet_shared').set({
        workspaceId: 'khiem_thi_viet_shared',
        shopName: 'KHIẾM THỊ VIỆT',
        payloadJson: '{"taiKhoans":[]}',
        accountsCount: 1,
        customersCount: 0,
        ticketsCount: 0,
        updatedByUid: 'admin_uid_1',
        updatedByName: 'Admin Quản trị',
        updatedAt: new Date(),
      })
    );

    await assertFails(adminDb.doc('spa_workspaces/khiem_thi_viet_shared').delete());
  });
});

describe('users profile security rules', () => {
  test('allows owner to create/read own profile and denies reading/writing another user profile', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_uid').firestore();
    const bobDb = testEnv.authenticatedContext('bob_uid').firestore();

    await assertSucceeds(
      aliceDb.doc('users/alice_uid').set({
        uid: 'alice_uid',
        displayName: 'Alice Admin',
        email: 'alice@example.com',
        spaPhone: '0397772963',
        spaRole: 'ADMIN',
        updatedAt: new Date(),
      })
    );

    await assertSucceeds(aliceDb.doc('users/alice_uid').get());
    await assertFails(bobDb.doc('users/alice_uid').get());
    await assertFails(
      bobDb.doc('users/alice_uid').set({
        uid: 'alice_uid',
        displayName: 'Hacked',
        email: 'bob@example.com',
        spaPhone: '0912345678',
        spaRole: 'STAFF',
        updatedAt: new Date(),
      })
    );
  });
});
