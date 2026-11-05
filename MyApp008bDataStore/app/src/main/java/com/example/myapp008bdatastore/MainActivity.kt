package com.example.myapp008bdatastore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.myapp008bdatastore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnDashboard.setOnClickListener {
            showFragment(DashboardFragment())
            updateNavigation(isDashboardSelected = true)
        }
        binding.btnSettings.setOnClickListener {
            showFragment(SettingsFragment())
            updateNavigation(isDashboardSelected = false)
        }

        if (savedInstanceState == null) {
            showFragment(DashboardFragment())
            updateNavigation(isDashboardSelected = true)
        } else {
            updateNavigation(
                isDashboardSelected = supportFragmentManager
                    .findFragmentById(R.id.fragmentContainer) is DashboardFragment
            )
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateNavigation(isDashboardSelected: Boolean) {
        binding.btnDashboard.isEnabled = !isDashboardSelected
        binding.btnSettings.isEnabled = isDashboardSelected
    }
}
