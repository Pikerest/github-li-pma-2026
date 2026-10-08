package com.example.myapp004objednavka

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp004objednavka.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.quantityNumberPicker.apply {
            minValue = MIN_QUANTITY
            maxValue = MAX_QUANTITY
            value = MIN_QUANTITY
            wrapSelectorWheel = false
        }

        binding.heroRadioGroup.setOnCheckedChangeListener { _, _ -> updateHeroImage() }
        binding.orderButton.setOnClickListener { createOrderSummary() }
        updateHeroImage()
    }

    private fun updateHeroImage() {
        val hero = selectedHero()
        binding.heroImageView.setImageResource(resolveHeroImage(hero))
        binding.heroImageView.contentDescription = getString(
            R.string.hero_portrait_description,
            getString(hero.nameResource)
        )
    }

    private fun resolveHeroImage(hero: Hero): Int {
        val downloadedImage = resources.getIdentifier(hero.imageName, "drawable", packageName)
        return downloadedImage.takeIf { it != 0 } ?: hero.fallbackImageResource
    }

    private fun createOrderSummary() {
        val hero = selectedHero()
        val quantity = binding.quantityNumberPicker.value
        val extras = selectedExtras()
        val totalPrice = (hero.price + extras.sumOf { it.price }) * quantity
        val extrasText = if (extras.isEmpty()) {
            getString(R.string.extras_none)
        } else {
            extras.joinToString(getString(R.string.extras_separator)) {
                getString(it.nameResource)
            }
        }

        binding.summaryTextView.text = getString(
            R.string.order_summary,
            getString(hero.nameResource),
            quantity,
            extrasText,
            totalPrice
        )

        Toast.makeText(
            this,
            getString(
                R.string.order_confirmation,
                quantity,
                getString(hero.nameResource),
                totalPrice
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun selectedHero(): Hero = when (binding.heroRadioGroup.checkedRadioButtonId) {
        R.id.hoodwinkRadioButton -> Hero(
            R.string.hero_hoodwink,
            "hoodwink",
            android.R.drawable.ic_menu_compass,
            HOODWINK_PRICE
        )

        R.id.legionCommanderRadioButton -> Hero(
            R.string.hero_legion_commander,
            "legion_commander",
            android.R.drawable.ic_menu_myplaces,
            LEGION_COMMANDER_PRICE
        )

        else -> Hero(
            R.string.hero_dragon_knight,
            "dragon_knight",
            android.R.drawable.ic_menu_gallery,
            DRAGON_KNIGHT_PRICE
        )
    }

    private fun selectedExtras(): List<Extra> = buildList {
        if (binding.arcanaCheckBox.isChecked) {
            add(Extra(R.string.extra_arcana, ARCANA_PRICE))
        }
        if (binding.immortalCheckBox.isChecked) {
            add(Extra(R.string.extra_immortal, IMMORTAL_PRICE))
        }
        if (binding.courierCheckBox.isChecked) {
            add(Extra(R.string.extra_courier, COURIER_PRICE))
        }
    }

    private data class Hero(
        val nameResource: Int,
        val imageName: String,
        val fallbackImageResource: Int,
        val price: Int
    )

    private data class Extra(
        val nameResource: Int,
        val price: Int
    )

    companion object {
        private const val MIN_QUANTITY = 1
        private const val MAX_QUANTITY = 5
        private const val DRAGON_KNIGHT_PRICE = 1_200
        private const val HOODWINK_PRICE = 950
        private const val LEGION_COMMANDER_PRICE = 1_400
        private const val ARCANA_PRICE = 500
        private const val IMMORTAL_PRICE = 300
        private const val COURIER_PRICE = 150
    }
}
