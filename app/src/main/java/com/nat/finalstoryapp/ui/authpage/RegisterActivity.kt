package com.nat.finalstoryapp.ui.authpage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.nat.finalstoryapp.data.api.ApiServiceFactory
import com.nat.finalstoryapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(ApiServiceFactory.create())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySequentialFadeInAnimation()

        binding.buttonRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            showProgressBar(true)
            authViewModel.register(name, email, password).observe(this, Observer { result ->
                showProgressBar(false)
                result.onSuccess {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                result.onFailure {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.textViewLinkToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun applySequentialFadeInAnimation() {
        val views = listOf(
            binding.imageView,
            binding.textViewRegister1,
            binding.textViewRegister2,
            binding.edRegisterName,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.buttonRegister,
            binding.textViewLinkToLogin
        )

        val animatorSet = AnimatorSet()
        val animators = views.mapIndexed { index, view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                duration = 200
                startDelay = index * 100L
            }
        }
        animatorSet.playSequentially(animators)
        animatorSet.start()
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }
}