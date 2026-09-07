package com.example.billreminder.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billreminder.R
import com.example.billreminder.databinding.ActivityRegisterBinding
import com.example.billreminder.util.ViewModelFactory
import com.example.billreminder.util.app
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory { AuthViewModel(app.authRepository) })[AuthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.editUsername.text?.toString().orEmpty(),
                binding.editPassword.text?.toString().orEmpty(),
                binding.editConfirmPassword.text?.toString().orEmpty()
            )
        }

        binding.btnGoLogin.setOnClickListener { finish() }

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state is AuthUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnRegister.isEnabled = state !is AuthUiState.Loading

            when (state) {
                is AuthUiState.Success -> {
                    Snackbar.make(binding.root, R.string.registration_success, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is AuthUiState.Error -> {
                    val message = when (state.reason) {
                        "empty_field" -> getString(R.string.error_field_required)
                        "password_too_short" -> getString(R.string.error_password_too_short)
                        "password_mismatch" -> getString(R.string.error_password_mismatch)
                        "username_taken" -> getString(R.string.error_username_taken)
                        else -> getString(R.string.error_database)
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
