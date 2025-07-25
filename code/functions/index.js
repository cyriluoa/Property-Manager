const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// 1. Delete user by UID
exports.deleteUserByUid = functions.https.onCall(async (request) => {
    const { uid } = request.data || {};

    if (!uid) {
        throw new functions.https.HttpsError(
            "invalid-argument",
            "The function must be called with a UID."
        );
    }

    try {
        await admin.auth().deleteUser(uid);
        console.log(`Successfully deleted auth user with UID: ${uid}`);
        return { success: true };
    } catch (error) {
        console.error(`Error deleting user with UID ${uid}:`, error);
        throw new functions.https.HttpsError("internal", "Failed to delete user.", error);
    }
});

// 2. Create user via Admin SDK (no session change)
exports.createUserWithEmail = functions.https.onCall(async (request) => {
    const { email, password } = request.data || {};

    if (!email || !password) {
        throw new functions.https.HttpsError("invalid-argument", "Email and password are required.");
    }

    try {
        const userRecord = await admin.auth().createUser({ email, password });
        console.log(`User created: ${userRecord.uid}`);
        return { uid: userRecord.uid };
    } catch (error) {
        console.error("Failed to create user:", error);
        throw new functions.https.HttpsError("internal", "User creation failed.", error);
    }
});

