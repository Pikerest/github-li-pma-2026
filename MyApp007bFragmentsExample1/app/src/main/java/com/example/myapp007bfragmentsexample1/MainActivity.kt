package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.myapp007bfragmentsexample1.databinding.ActivityMainBinding

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

        // Při prvním spuštění zobrazíme fragment s hrdiny.
        if (savedInstanceState == null) {
            showFragment(HeroesFragment())
        }

        // Po otočení telefonu zvýrazníme tlačítko právě obnoveného fragmentu.
        val checkedButton = when (
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        ) {
            is ItemsFragment -> R.id.btnItems
            is TipsFragment -> R.id.btnTips
            else -> R.id.btnHeroes
        }
        binding.navigationGroup.check(checkedButton)

        binding.navigationGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnHeroes -> showFragment(HeroesFragment())
                R.id.btnItems -> showFragment(ItemsFragment())
                R.id.btnTips -> showFragment(TipsFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
