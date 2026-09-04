package com.example.smartexpensemanager.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartexpensemanager.databinding.ActivitySplashBinding
import com.example.smartexpensemanager.ui.auth.LoginActivity
import com.example.smartexpensemanager.ui.dashboard.DashboardActivity
import com.example.smartexpensemanager.ui.onboarding.OnboardingActivity
import com.example.smartexpensemanager.util.app
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(500)
            val destination = when {
                app.sessionManager.isLoggedIn -> DashboardActivity::class.java
                !app.preferencesManager.hasCompletedOnboarding -> OnboardingActivity::class.java
                else -> LoginActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, destination))
            finish()
        }
    }
}
