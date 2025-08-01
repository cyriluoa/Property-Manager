const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const moment = require("moment-timezone");

admin.initializeApp();

// 1. Activate contracts at start date
exports.activateContractsAtStartDate = onSchedule(
  {
    schedule: "0 8 * * *", // 8 AM IST
    timeZone: "Asia/Kolkata",
  },
  async () => {
    logger.log("🔔 Triggered: activateContractsAtStartDate");

    const propertySnapshot = await admin.firestore().collection("properties").get();
    logger.log(`📦 Retrieved ${propertySnapshot.size} properties`);

    for (const propertyDoc of propertySnapshot.docs) {
      const propertyId = propertyDoc.id;
      const propertyData = propertyDoc.data();

      logger.log(`➡️ Checking property: ${propertyId}`);

      const contractId = propertyData.currentContractId;
      if (!contractId) {
        logger.log(`⚠️ Property ${propertyId} has no currentContractId. Skipping.`);
        continue;
      }

      const contractRef = admin
        .firestore()
        .collection("properties")
        .doc(propertyId)
        .collection("contracts")
        .doc(contractId);

      const contractDoc = await contractRef.get();
      if (!contractDoc.exists) {
        logger.log(`❌ Contract ${contractId} does not exist under property ${propertyId}. Skipping.`);
        continue;
      }

      const contract = contractDoc.data();
      logger.log(`📄 Contract ${contractId} for property ${propertyId} retrieved`);

      if (contract.contractState !== "ACCEPTED") {
        logger.log(`⛔ Contract ${contractId} is not in ACCEPTED state (${contract.contractState}). Skipping.`);
        continue;
      }

      const today = moment().tz("Asia/Kolkata").startOf("day");
      const startDate = moment.tz(contract.startDate, "DD-MM-YYYY", "Asia/Kolkata").startOf("day");

      logger.log(`📆 Comparing dates: today=${today.format("DD-MM-YYYY")}, startDate=${startDate.format("DD-MM-YYYY")}`);

      if (today.isSameOrAfter(startDate)) {
        logger.log(`✅ Activating contract ${contractId}...`);
        await contractRef.update({ contractState: "ACTIVE" });

        logger.log(`🎉 Activated contract ${contractId} for property ${propertyId}`);
        await sendContractNotification(
          contract.clientId,
          propertyData.ownerId,
          "started",
          propertyData.name
        );
      } else {
        logger.log(`⏩ Contract ${contractId} start date is in the future. Skipping activation.`);
      }
    }

    logger.log("🏁 Finished: activateContractsAtStartDate");
    return null;
  }
);



// 2. End expired contracts
// 2. End expired contracts
exports.endExpiredContracts = onSchedule(
  {
    schedule: "5 8 * * *", // 8:05 AM IST
    timeZone: "Asia/Kolkata",
  },
  async () => {
    logger.log("🔔 Triggered: endExpiredContracts");

    const propertySnapshot = await admin.firestore().collection("properties").get();
    logger.log(`📦 Retrieved ${propertySnapshot.size} properties`);

    for (const propertyDoc of propertySnapshot.docs) {
      const propertyId = propertyDoc.id;
      const propertyData = propertyDoc.data();

      logger.log(`➡️ Checking property: ${propertyId}`);

      const contractId = propertyData.currentContractId;
      if (!contractId) {
        logger.log(`⚠️ Property ${propertyId} has no currentContractId. Skipping.`);
        continue;
      }

      const contractRef = admin
        .firestore()
        .collection("properties")
        .doc(propertyId)
        .collection("contracts")
        .doc(contractId);

      const contractDoc = await contractRef.get();
      if (!contractDoc.exists) {
        logger.log(`❌ Contract ${contractId} does not exist. Skipping.`);
        continue;
      }

      const contract = contractDoc.data();
      logger.log(`📄 Contract ${contractId} retrieved`);

      if (contract.contractState !== "ACTIVE") {
        logger.log(`⛔ Contract ${contractId} is not ACTIVE (${contract.contractState}). Skipping.`);
        continue;
      }

      const today = moment().tz("Asia/Kolkata").startOf("day");
      const endDate = moment.tz(contract.endDate, "DD-MM-YYYY", "Asia/Kolkata").startOf("day");

      logger.log(`📆 Comparing dates: today=${today.format("DD-MM-YYYY")}, endDate=${endDate.format("DD-MM-YYYY")}`);

      if (today.isSameOrAfter(endDate)) {
        logger.log(`✅ Ending contract ${contractId}...`);

        await contractRef.update({ contractState: "OVER" });

        await propertyDoc.ref.update({
          currentContractId: null,
          status: "VACANT",
        });

        logger.log(`🏁 Marked contract ${contractId} as OVER for property ${propertyId}`);
        await sendContractNotification(
          contract.clientId,
          propertyData.ownerId,
          "ended",
          propertyData.name
        );
      } else {
        logger.log(`⏩ Contract ${contractId} end date is in the future. Skipping.`);
      }
    }

    logger.log("✅ Finished: endExpiredContracts");
    return null;
  }
);


// Shared notification function
async function sendContractNotification(clientId, ownerId, status, propertyName) {
  const messaging = admin.messaging();
  const notificationText = `Your contract for "${propertyName}" has ${status}.`;
  const users = [clientId, ownerId];

  for (const uid of users) {
    try {
      const userDoc = await admin.firestore().collection("users").doc(uid).get();
      const userData = userDoc.data();
      const tokens = userData?.fcmTokens || [];

      if (tokens.length === 0) continue;

      const payload = {
        notification: {
          title: "Contract Update",
          body: notificationText,
        },
        data: {
          type: "contract_status",
          propertyName,
          status,
        },
      };

      const invalidTokens = [];

      for (const token of tokens) {
        try {
          await messaging.send({ token, ...payload });
          logger.log(`Sent contract ${status} notification to ${uid}`);
        } catch (err) {
          logger.error(`Error sending to token ${token}`, err);
          invalidTokens.push(token);
        }
      }

      if (invalidTokens.length > 0) {
        await admin.firestore().collection("users").doc(uid).update({
          fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
        });
        logger.log(`Removed invalid tokens from user ${uid}`);
      }
    } catch (err) {
      logger.error(`Failed to notify user ${uid}:`, err);
    }
  }
}
