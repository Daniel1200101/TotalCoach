package com.example.totalcoach.models

data class Trainee(
    val id: String = "",
    val personalInfo: PersonalInfo? = null
)

data class PersonalInfo(
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val height: Double? = null,
    val email: String? = null,
    val location: String? = null,
    val phone: String? = null
)
