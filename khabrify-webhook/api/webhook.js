const admin = require("firebase-admin");

// Initialize Firebase using Environment Variables to keep your key secure
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert({
            projectId: process.env.FIREBASE_PROJECT_ID,
            clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
            // Vercel sometimes escapes newlines in keys, this ensures they are formatted correctly
            privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
        })
    });
}

module.exports = async (req, res) => {
    // 1. Ensure it's a POST request
    if (req.method !== "POST") {
        return res.status(405).json({ error: "Method Not Allowed" });
    }

    // 2. Extract data sent by Google Apps Script
    const articleTitle = req.body.title || "New breaking story!";
    const articleUrl = req.body.link || "";

    // 3. Construct the FCM Payload (DATA-ONLY FORMAT)
    const message = {
        // We completely removed the 'notification' block.
        // Everything goes inside 'data'. (Note: values must be strings)
        data: {
            title: "Khabrify: Breaking News",
            body: articleTitle,
            url: articleUrl
        },
        android: {
            priority: "high"
        },
        topic: "news"
    };

    // 4. Send the message
    try {
        const response = await admin.messaging().send(message);
        console.log("Successfully sent message:", response);
        return res.status(200).json({ success: true, message: "Notification sent successfully!" });
    } catch (error) {
        console.error("Error sending message:", error);
        return res.status(500).json({ error: "Error sending notification." });
    }
};