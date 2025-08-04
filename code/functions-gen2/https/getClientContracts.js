const { onCall } = require("firebase-functions/v2/https");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");

const getClientContracts = onCall(async (request) => {
  const clientId = request.auth?.uid || request.data.clientId;

  if (!clientId) {
    throw new functions.https.HttpsError("unauthenticated", "Client ID is required");
  }

  const db = admin.firestore();
  const result = [];

  logger.log("🔍 Starting to fetch contracts for client:", clientId);

  const propertySnap = await db.collection("properties").get();
  logger.log(`📦 Fetched ${propertySnap.size} properties`);

  for (const propertyDoc of propertySnap.docs) {
    const propertyId = propertyDoc.id;
    const propertyData = propertyDoc.data();

    const contractSnap = await db
      .collection("properties")
      .doc(propertyId)
      .collection("contracts")
      .where("clientId", "==", clientId)
      .get();

    logger.log(`🏠 Property ${propertyId} → ${contractSnap.size} contracts found`);

    for (const contractDoc of contractSnap.docs) {
      const contractData = contractDoc.data();

      if (
        contractData.contractState === "PENDING" ||
        contractData.contractState === "NOT_ACCEPTED_IN_TIME"
      ) {
        logger.log(`⛔ Skipping contract ${contractDoc.id} with state ${contractData.contractState}`);
        continue;
      }

      result.push({
        // Property fields
        propertyId,
        propertyName: propertyData.name,
        propertyImageUrl: propertyData.imageUrl || null,
        ownerId: propertyData.ownerId,
        ownerName: "", // optional
        propertyStatus: propertyData.status,
        propertyCreatedAt: propertyData.createdAt,
        propertyUpdatedAt: propertyData.updatedAt,

        // Contract fields
        contractId: contractDoc.id,
        clientId: contractData.clientId,
        startDate: contractData.startDate,
        endDate: contractData.endDate,
        contractLengthMonths: contractData.contractLengthMonths,
        monthlyRentBreakdown: contractData.monthlyRentBreakdown || [],
        preContractOverdueAmounts: contractData.preContractOverdueAmounts || [],
        contractCreatedAt: contractData.createdAt,
        contractNotes: contractData.notes || "",
        contractState: contractData.contractState,
      });
    }
  }

  logger.log(`✅ Done! Returning ${result.length} client contracts`);
  return result;
});

module.exports = { getClientContracts };
