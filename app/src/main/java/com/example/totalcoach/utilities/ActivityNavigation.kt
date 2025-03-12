package com.example.totalcoach.utilities

import android.content.Context
import android.content.Intent
import com.example.totalcoach.LoginActivity
import com.example.totalcoach.TrainerMainActivity
import com.example.totalcoach.PersonalDetailsActivity
import com.example.totalcoach.SignUpActivity
import com.example.totalcoach.TraineeMainActivity

class ActivityNavigation(private val context: Context) {

    fun navigateToTrainerMainActivity() {
        val intent = Intent(context, TrainerMainActivity::class.java)
        context.startActivity(intent)
    }
    fun navigateToTraineeMainActivity() {
        val intent = Intent(context, TraineeMainActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToPersonalDetails() {
        val intent = Intent(context, PersonalDetailsActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToSignUp() {
        val intent = Intent(context, SignUpActivity::class.java)
        context.startActivity(intent)
    }
    fun navigateToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}
