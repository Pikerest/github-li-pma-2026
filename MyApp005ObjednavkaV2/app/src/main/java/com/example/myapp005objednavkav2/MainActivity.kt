package com.example.myapp005objednavkav2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp005objednavkav2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rgHeroes.setOnCheckedChangeListener { _, _ -> updateOrderPreview() }
        binding.cbArcana.setOnCheckedChangeListener { _, _ -> updatePrice() }
        binding.cbImmortal.setOnCheckedChangeListener { _, _ -> updatePrice() }
        binding.cbCourier.setOnCheckedChangeListener { _, _ -> updatePrice() }
        binding.btnOrder.setOnClickListener { openOrderSummary() }

        updateOrderPreview()
    }

    private fun selectedHeroPrice(): Int = when (binding.rgHeroes.checkedRadioButtonId) {
        binding.rbDragonKnight.id -> DRAGON_KNIGHT_PRICE
        binding.rbHoodwink.id -> HOODWINK_PRICE
        binding.rbLegionCommander.id -> LEGION_COMMANDER_PRICE
        else -> 0
    }

    private fun selectedHeroName(): String = when (binding.rgHeroes.checkedRadioButtonId) {
        binding.rbDragonKnight.id -> getString(R.string.hero_dragon_knight)
        binding.rbHoodwink.id -> getString(R.string.hero_hoodwink)
        binding.rbLegionCommander.id -> getString(R.string.hero_legion_commander)
        else -> getString(R.string.unknown_hero)
    }

    private fun selectedHeroImage(): Int = when (binding.rgHeroes.checkedRadioButtonId) {
        binding.rbHoodwink.id -> R.drawable.hoodwink
        binding.rbLegionCommander.id -> R.drawable.legion_commander
        else -> R.drawable.dragon_knight
    }

    private fun calculateTotalPrice(): Int {
        var totalPrice = selectedHeroPrice()

        if (binding.cbArcana.isChecked) totalPrice += ARCANA_PRICE
        if (binding.cbImmortal.isChecked) totalPrice += IMMORTAL_PRICE
        if (binding.cbCourier.isChecked) totalPrice += COURIER_PRICE

        return totalPrice
    }

    private fun updateOrderPreview() {
        binding.ivHero.setImageResource(selectedHeroImage())
        binding.ivHero.contentDescription = getString(
            R.string.hero_portrait_description,
            selectedHeroName()
        )
        updatePrice()
    }

    private fun updatePrice() {
        binding.tvTotalPrice.text = getString(R.string.total_price, calculateTotalPrice())
    }

    private fun openOrderSummary() {
        val intent = Intent(this, SummaryActivity::class.java).apply {
            putExtra(SummaryActivity.EXTRA_HERO_NAME, selectedHeroName())
            putExtra(SummaryActivity.EXTRA_ARCANA, binding.cbArcana.isChecked)
            putExtra(SummaryActivity.EXTRA_IMMORTAL, binding.cbImmortal.isChecked)
            putExtra(SummaryActivity.EXTRA_COURIER, binding.cbCourier.isChecked)
            putExtra(SummaryActivity.EXTRA_TOTAL_PRICE, calculateTotalPrice())
        }
        startActivity(intent)
    }

    companion object {
        private const val DRAGON_KNIGHT_PRICE = 1_200
        private const val HOODWINK_PRICE = 950
        private const val LEGION_COMMANDER_PRICE = 1_400
        private const val ARCANA_PRICE = 500
        private const val IMMORTAL_PRICE = 300
        private const val COURIER_PRICE = 150
    }
}
