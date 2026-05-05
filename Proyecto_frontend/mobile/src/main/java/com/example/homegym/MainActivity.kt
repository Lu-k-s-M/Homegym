package com.example.homegym

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.view.KeyEvent
import com.example.homegym.util.CursorController
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.AuthRepository
import com.example.homegym.databinding.ActivityMainBinding
import com.example.homegym.ui.home.HomeActivity
import com.example.homegym.ui.login.LoginState
import com.example.homegym.ui.login.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var cursorController: CursorController
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursorController = CursorController(this)
        cursorController.attach()

        val repository = AuthRepository(RetrofitClient.instance)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(repository, applicationContext) as T
            }
        })[LoginViewModel::class.java]

        setupListeners()
        observeViewModel()

        binding.tietUsername.requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (cursorController.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.tietUsername.text.toString()
            val password = binding.tietPassword.text.toString()
            
            if (isRegisterMode) {
                val email = binding.tietEmail.text.toString()
                viewModel.register(username, password, email)
            } else {
                viewModel.login(username, password)
            }
        }

        binding.btnSwitchMode.setOnClickListener {
            toggleMode()
        }

        binding.btnGuest.setOnClickListener {
            viewModel.enterAsGuest()
        }
    }

    private fun toggleMode() {
        isRegisterMode = !isRegisterMode
        if (isRegisterMode) {
            binding.etEmail.visibility = View.VISIBLE
            binding.tvPasswordRequirements.visibility = View.VISIBLE
            binding.btnLogin.text = "Registrarse"
            binding.btnSwitchMode.text = "¿Ya tienes cuenta? Inicia Sesión"
            binding.tietEmail.requestFocus()
        } else {
            binding.etEmail.visibility = View.GONE
            binding.tvPasswordRequirements.visibility = View.GONE
            binding.btnLogin.text = "Iniciar Sesión"
            binding.btnSwitchMode.text = "¿No tienes cuenta? Regístrate"
            binding.tietUsername.requestFocus()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is LoginState.GuestSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Accediendo como invitado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is LoginState.RegisterSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Registro exitoso. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                    toggleMode() // Volver a modo login
                }
                is LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}