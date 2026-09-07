package com.example.billreminder.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.billreminder.databinding.ActivitySplashBinding
import com.example.billreminder.ui.auth.LoginActivity
import com.example.billreminder.ui.dashboard.DashboardActivity
import com.example.billreminder.util.app
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(500)
            val destination = if (app.sessionManager.isLoggedIn) {
                DashboardActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, destination))
            finish()
        }
    }
}
