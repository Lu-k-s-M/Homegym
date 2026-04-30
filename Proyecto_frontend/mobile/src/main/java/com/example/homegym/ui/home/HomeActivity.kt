package com.example.homegym.ui.home

import android.view.KeyEvent
import com.example.homegym.util.CursorController
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.homegym.MainActivity
import com.example.homegym.R
import com.example.homegym.databinding.ActivityHomeBinding
import com.example.homegym.ui.perfil.PerfilFragment
import com.example.homegym.ui.rutinas.RutinasFragment
import com.example.homegym.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.core.widget.addTextChangedListener
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.EjercicioRepository

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var cursorController: CursorController
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = EjercicioRepository(RetrofitClient.instance)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repository, applicationContext) as T
            }
        })[HomeViewModel::class.java]

        cursorController = CursorController(this)
        cursorController.attach()

        setupNavigation()
        setupBackPressed()
        setupSearch()

        if (savedInstanceState == null) {
            replaceFragment(EjerciciosFragment(), "Ejercicios")
        }
    }

    fun setSearchButtonVisibility(visible: Boolean) {
        binding.btnSearch.visibility = if (visible) View.VISIBLE else View.GONE
        binding.etSearch.visibility = View.GONE
        binding.etSearch.setText("")
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            if (binding.etSearch.visibility == View.GONE) {
                binding.etSearch.visibility = View.VISIBLE
                binding.etSearch.requestFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            } else {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    binding.etSearch.visibility = View.GONE
                    binding.etSearch.clearFocus()
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                }
            }
        }

        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etSearch.text.isEmpty()) {
                binding.etSearch.visibility = View.GONE
            }
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                binding.etSearch.clearFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        viewModel.filterEjercicios(query)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (currentFragment !is SearchFragment) {
            replaceFragment(SearchFragment(), "Búsqueda")
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (cursorController.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private var lastBackPressTime = 0L

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return
                }

                val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                if (currentFragment !is EjerciciosFragment) {
                    replaceFragment(EjerciciosFragment(), "Ejercicios")
                    binding.navView.setCheckedItem(R.id.nav_ejercicios)
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(this@HomeActivity, "Pulsa atrás de nuevo para salir", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_ejercicios -> {
                    replaceFragment(EjerciciosFragment(), "Ejercicios")
                }
                R.id.nav_rutinas -> {
                    replaceFragment(RutinasFragment(), "Rutinas")
                }
                R.id.nav_recientes -> {
                    replaceFragment(RecientesFragment(), "Recientes")
                }
                R.id.nav_perfil -> {
                    replaceFragment(PerfilFragment(), "Mi Perfil")
                }
                R.id.nav_ajustes -> {
                    Toast.makeText(this, "Ajustes no implementado", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_faq -> {
                    Toast.makeText(this, "FAQ no implementado", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_theme -> {
                    toggleTheme()
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun toggleTheme() {
        val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        if (currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            TokenManager.deleteToken(applicationContext)
            startActivity(Intent(this@HomeActivity, MainActivity::class.java))
            finish()
        }
    }
}
