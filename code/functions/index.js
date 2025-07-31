const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/firestore");
const { logger } = require("firebase-functions");
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// 1. Delete user by UID
exports.deleteUserByUid = functions.https.onCall(async (request) => {
    const data = typeof request === "object" && request.data ? request.data : request;
    const { uid } = data || {};

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
    const data = typeof request === "object" && request.data ? request.data : request;
    const { email, password } = data || {};

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

exports.notifyClientOnRequest = onDocumentCreated("client_requests/{requestId}", async (event) => {
    logger.log("New client request detected.");

    const requestData = event.data.data();
    logger.log("Request data:", requestData);

    const clientId = requestData.clientId;
    const ownerName = requestData.ownerName || "An owner";
    const propertyName = requestData.propertyName || "a property";

    if (!clientId) {
        logger.error("Missing clientId in request.");
        return;
    }

    logger.log(`Fetching FCM tokens for clientId: ${clientId}`);

    const clientDoc = await admin.firestore().collection("users").doc(clientId).get();
    const clientData = clientDoc.data();

    if (!clientData || !clientData.fcmTokens) {
        logger.warn(`No client data or FCM tokens for user ${clientId}. Skipping notification.`);
        return;
    }

    const tokens = clientData.fcmTokens;
    logger.log("Tokens found:", tokens);

    if (tokens.length === 0) {
        logger.warn(`No FCM tokens found for user ${clientId}. Skipping notification.`);
        return;
    }

    const payload = {
        notification: {
            title: "Client Request Received",
            body: `${ownerName} invited you to join "${propertyName}".`,
        },
        data: {
            requestId: event.params.requestId,
            type: "client_request",
        },
    };

    const invalidTokens = [];

    const messaging = admin.messaging();

    for (const token of tokens) {
        try {
            const res = await messaging.send({
                token,
                notification: {
                    title: "Client Request Received",
                    body: `${ownerName} invited you to join "${propertyName}".`,
                },
                data: {
                    requestId: event.params.requestId,
                    type: "client_request",
                },
            });
            logger.log(`Notification sent to token: ${token}. Response: ${res}`);
        } catch (err) {
            logger.error(`Error sending to token ${token}:`, err);
            invalidTokens.push(token);
        }
    }


    if (invalidTokens.length > 0) {
        logger.log("Removing invalid tokens:", invalidTokens);
        await admin.firestore().collection("users").doc(clientId).update({
            fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
        });
    }
});


// 4. Notify owner when status changes to accepted/denied
exports.notifyOwnerOnStatusChange = onDocumentUpdated("client_requests/{requestId}", async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();

    if (!before || !after) {
        logger.warn("Missing document data before or after update.");
        return;
    }

    const statusChanged = before.status === "pending" &&
        (after.status === "accepted" || after.status === "denied");

    if (!statusChanged) {
        logger.log("Status didn't change from pending to accepted/denied. Skipping notification.");
        return;
    }

    const { clientId, ownerId, propertyName, status } = after;

    if (!clientId || !ownerId) {
        logger.error("Missing clientId or ownerId in request.");
        return;
    }

    const clientDoc = await admin.firestore().collection("users").doc(clientId).get();
    const clientData = clientDoc.data();
    const clientUsername = clientData?.username || "A client";

    const ownerDoc = await admin.firestore().collection("users").doc(ownerId).get();
    const ownerData = ownerDoc.data();
    const tokens = ownerData?.fcmTokens;

    if (!tokens || tokens.length === 0) {
        logger.warn(`No FCM tokens for owner ${ownerId}. Skipping notification.`);
        return;
    }

    const payload = {
        notification: {
            title: "Request Status Updated",
            body: `${clientUsername} ${status} your request for "${propertyName}".`,
        },
        data: {
            requestId: event.params.requestId,
            type: "status_update",
            status,
        },
    };

    const messaging = admin.messaging();
    const invalidTokens = [];

    for (const token of tokens) {
        try {
            const res = await messaging.send({
                token,
                notification: payload.notification,
                data: payload.data,
            });
            logger.log(`Notification sent to token ${token}: ${res}`);
        } catch (err) {
            logger.error(`Error sending to token ${token}:`, err);
            invalidTokens.push(token);
        }
    }

    if (invalidTokens.length > 0) {
        logger.log("Removing invalid tokens from owner's record:", invalidTokens);
        await admin.firestore().collection("users").doc(ownerId).update({
            fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
        });
    }
});