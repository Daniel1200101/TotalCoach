package com.example.totalcoach.models

data class PaymentRecord(
    val clientName: String,
    val trainingType: String,
    val date: String,
    val amount: Double,
    val status: String
)