const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const moment = require("moment-timezone");

const { onCall } = require("firebase-functions/v2/https");
const { getClientContracts } = require("./https/getClientContracts");


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

       if (state !== "ACTIVE" && state !== "COMPLETELY_PAID_OFF") {
        logger.log(`⛔ Contract ${contractId} is ${state}. Skipping.`);
        continue;
      }

      const today = moment().tz("Asia/Kolkata").startOf("day");
      const endDate = moment.tz(contract.endDate, "DD-MM-YYYY", "Asia/Kolkata").startOf("day");

      logger.log(`📆 Comparing dates: today=${today.format("DD-MM-YYYY")}, endDate=${endDate.format("DD-MM-YYYY")}`);

      if (today.isSameOrAfter(endDate)) {
        logger.log(`✅ Ending contract ${contractId}...`);

        if (state === "ACTIVE") {
          await contractRef.update({ contractState: "OVER" });
          logger.log(`📝 Marked contract ${contractId} as OVER`);
        }

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




exports.markPayablesDue = onSchedule(
  {
    schedule: "0 10 * * *", // 10:00 AM IST
    timeZone: "Asia/Kolkata",
  },
  async () => {
    logger.log("🔔 Triggered: markPayablesDue");

    const properties = await admin.firestore().collection("properties").get();
    logger.log(`📦 Fetched ${properties.size} properties`);

    for (const propertyDoc of properties.docs) {
      const propertyId = propertyDoc.id;
      const propertyData = propertyDoc.data();

      if (!propertyData.currentContractId) continue;

      const contractRef = admin
        .firestore()
        .collection("properties")
        .doc(propertyId)
        .collection("contracts")
        .doc(propertyData.currentContractId);

      const contractDoc = await contractRef.get();
      if (!contractDoc.exists) continue;

      const contract = contractDoc.data();
      if (contract.contractState !== "ACTIVE") continue;

      const payableSnapshot = await contractRef.collection("payableItems").get();
      logger.log(`➡️ ${propertyId} → ${payableSnapshot.size} payable items found`);

      for (const item of payableSnapshot.docs) {
        const itemData = item.data();
        if (itemData.status === "NOT_APPLIED_YET") {
          if (shouldUpdateStatus(itemData.startDate)) {
            logger.log(`✅ Updating item ${item.id} to DUE`);
            await item.ref.update({ status: "DUE" });
            await sendPayableStatusNotification(itemData, "DUE");
          }
        }
      }
    }

    logger.log("✅ Completed: markPayablesDue");
    return null;
  }
);



exports.markPayablesOverdue = onSchedule(
  {
    schedule: "05 10 * * *", // 10:05 AM IST
    timeZone: "Asia/Kolkata",
  },
  async () => {
    logger.log("🔔 Triggered: markPayablesOverdue");

    const properties = await admin.firestore().collection("properties").get();
    logger.log(`📦 Fetched ${properties.size} properties`);

    for (const propertyDoc of properties.docs) {
      const propertyId = propertyDoc.id;
      const propertyData = propertyDoc.data();

      if (!propertyData.currentContractId) continue;

      const contractRef = admin
        .firestore()
        .collection("properties")
        .doc(propertyId)
        .collection("contracts")
        .doc(propertyData.currentContractId);

      const contractDoc = await contractRef.get();
      if (!contractDoc.exists) continue;

      const contract = contractDoc.data();
      if (contract.contractState !== "ACTIVE") continue;

      const payableSnapshot = await contractRef.collection("payableItems").get();
      logger.log(`➡️ ${propertyId} → ${payableSnapshot.size} payable items found`);

      for (const item of payableSnapshot.docs) {
        const itemData = item.data();
        if (itemData.status === "DUE") {
          if (shouldUpdateStatus(itemData.dueDate)) {
            logger.log(`⚠️ Marking item ${item.id} as OVERDUE`);
            await item.ref.update({ status: "OVERDUE" });
            await sendPayableStatusNotification(itemData, "OVERDUE");
          }
        }
      }
    }

    logger.log("✅ Completed: markPayablesOverdue");
    return null;
  }
);



async function sendPayableStatusNotification(item, newStatus) {
  const notificationText = `This month's rent is now ${newStatus}.`;

  const users = [item.clientId, item.ownerId];
  const messaging = admin.messaging();

  for (const uid of users) {
    try {
      const userDoc = await admin.firestore().collection("users").doc(uid).get();
      const tokens = userDoc.data()?.fcmTokens || [];

      if (tokens.length === 0) continue;

      const payload = {
        notification: {
          title: `Payable ${newStatus}`,
          body: notificationText,
        },
        data: {
          type: "payable_status",
          status: newStatus,
          payableId: item.id,
        },
      };

      const invalidTokens = [];

      for (const token of tokens) {
        try {
          await messaging.send({ token, ...payload });
          logger.log(`📩 Notification sent to ${uid} (${newStatus})`);
        } catch (err) {
          logger.error(`❌ Failed to send to ${token}`, err);
          invalidTokens.push(token);
        }
      }

      if (invalidTokens.length > 0) {
        await admin
          .firestore()
          .collection("users")
          .doc(uid)
          .update({
            fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
          });
        logger.log(`🧹 Removed invalid tokens for user ${uid}`);
      }
    } catch (err) {
      logger.error(`💥 Error notifying user ${uid}:`, err);
    }
  }
}





/**
 * Checks if a PayableItem should update status based on today's date and item date.
 * @param {string} itemDateStr - date in "DD-MM-YYYY" format
 * @param {string} comparison - "onOrAfter" | "before"
 * @returns {boolean}
 */
function shouldUpdateStatus(itemDateStr) {
  const today = moment().tz("Asia/Kolkata").startOf("day");
  const itemDate = moment.tz(itemDateStr, "DD-MM-YYYY", "Asia/Kolkata").startOf("day");

  return today.isSameOrAfter(itemDate);
}


exports.getClientContracts = getClientContracts;



