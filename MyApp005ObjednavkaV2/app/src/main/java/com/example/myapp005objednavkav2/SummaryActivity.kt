package com.example.myapp005objednavkav2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp005objednavkav2.databinding.ActivitySummaryBinding

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showOrderSummary()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun showOrderSummary() {
        val heroName = intent.getStringExtra(EXTRA_HERO_NAME)
            ?: getString(R.string.unknown_hero)
        val hasArcana = intent.getBooleanExtra(EXTRA_ARCANA, false)
        val hasImmortal = intent.getBooleanExtra(EXTRA_IMMORTAL, false)
        val hasCourier = intent.getBooleanExtra(EXTRA_COURIER, false)
        val totalPrice = intent.getIntExtra(EXTRA_TOTAL_PRICE, 0)

        val extras = buildList {
            if (hasArcana) add(getString(R.string.extra_arcana))
            if (hasImmortal) add(getString(R.string.extra_immortal))
            if (hasCourier) add(getString(R.string.extra_courier))
        }
        val extrasText = extras.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n• ", prefix = "• ")
            ?: getString(R.string.no_extras)

        binding.tvSelectedHero.text = getString(R.string.summary_selected_hero, heroName)
        binding.tvExtras.text = getString(R.string.summary_extras, extrasText)
        binding.tvFinalPrice.text = getString(R.string.final_price, totalPrice)
    }

    companion object {
        const val EXTRA_HERO_NAME = "extra_hero_name"
        const val EXTRA_ARCANA = "extra_arcana"
        const val EXTRA_IMMORTAL = "extra_immortal"
        const val EXTRA_COURIER = "extra_courier"
        const val EXTRA_TOTAL_PRICE = "extra_total_price"
    }
}
