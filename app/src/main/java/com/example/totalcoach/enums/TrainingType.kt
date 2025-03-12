package com.example.totalcoach.enums

enum class TrainingType(val displayName: String) {
    PERSONAL("Personal Training"),
    ONLINE("Online Training"),
    GROUP("Group Training"),
    STRENGTH("Strength Training"),
    CARDIO("Cardio Workout"),
    YOGA("Yoga Session"),
    PILATES("Pilates Class"),
    CROSSFIT("CrossFit Workout"),
    BOXING("Boxing Training"),
    DANCE("Dance Workout");

    companion object {
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}
