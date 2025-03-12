const {onRequest} = require("firebase-functions/v2/https");
const nodemailer = require("nodemailer");

// Your Gmail credentials
const gmailEmail = "your-email@gmail.com";
const gmailPassword = "your-app-password"; // Use an App Password if using Gmail

// Configure email transporter
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

exports.sendEmail = onRequest(async (req, res) => {
  const {email, code} = req.body;

  const mailOptions = {
    from: `"Your App Name" <${gmailEmail}>`,
    to: email,
    subject: "Your Access Code",
    text: `Hello, your access code is: ${code}`,
  };

  try {
    await transporter.sendMail(mailOptions);
    res.status(200).send({success: true});
  } catch (error) {
    res.status(500).send({success: false, error: error.message});
  }
});
