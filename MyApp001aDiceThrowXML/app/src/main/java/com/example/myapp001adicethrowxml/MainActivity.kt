package com.example.myapp001adicethrowxml

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val recentRolls = ArrayDeque<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val diceSymbols = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
        val tvDice = findViewById<TextView>(R.id.tvDice)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvAttempts = findViewById<TextView>(R.id.tvAttempts)
        val btnRoll = findViewById<Button>(R.id.btnRoll)
        val btnReset = findViewById<Button>(R.id.btnReset)

        // Překreslí součet i počet výsledků uložených v historii.
        fun updateCounter() {
            tvScore.text = getString(R.string.score_format, recentRolls.sum())
            tvAttempts.text = getString(R.string.attempts_format, recentRolls.size)
        }

        btnRoll.setOnClickListener {
            lifecycleScope.launch {
                btnRoll.isEnabled = false
                btnReset.isEnabled = false

                // Deset mezivýsledků vytvoří animaci trvající 2,5 sekundy.
                repeat(10) {
                    tvDice.text = diceSymbols.random()
                    delay(250)
                }

                val diceValue = (1..6).random()
                tvDice.text = diceSymbols[diceValue - 1]

                // Po jedenáctém hodu odebereme nejstarší výsledek.
                if (recentRolls.size == MAX_ROLLS) recentRolls.removeFirst()
                recentRolls.addLast(diceValue)
                updateCounter()

                btnRoll.isEnabled = true
                btnReset.isEnabled = true
            }
        }

        btnReset.setOnClickListener {
            recentRolls.clear()
            updateCounter()
        }
    }

    companion object {
        private const val MAX_ROLLS = 10
    }
}
