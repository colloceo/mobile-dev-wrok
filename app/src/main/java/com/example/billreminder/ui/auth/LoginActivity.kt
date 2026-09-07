package com.example.billreminder.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billreminder.R
import com.example.billreminder.databinding.ActivityLoginBinding
import com.example.billreminder.ui.dashboard.DashboardActivity
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory { AuthViewModel(app.authRepository) })[AuthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.editUsername.text?.toString().orEmpty(),
                binding.editPassword.text?.toString().orEmpty()
            )
        }

        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state is AuthUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnLogin.isEnabled = state !is AuthUiState.Loading

            when (state) {
                is AuthUiState.Success -> {
                    app.sessionManager.login(state.user.id, state.user.username)
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                is AuthUiState.Error -> {
                    val message = when (state.reason) {
                        "empty_field" -> getString(R.string.error_field_required)
                        "invalid_login" -> getString(R.string.error_invalid_login)
                        else -> getString(R.string.error_database)
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
