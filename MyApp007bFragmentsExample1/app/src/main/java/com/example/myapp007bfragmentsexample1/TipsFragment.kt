package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class TipsFragment : Fragment() {

    private val matchTips = listOf(
        "Vždy měj u sebe Town Portal Scroll. Rychlá pomoc týmu často rozhodne fight.",
        "Sleduj minimapu každých pár sekund. Chybějící soupeř může právě mířit na tvou linku.",
        "Před týmovým bojem zkontroluj důležité cooldowny a jestli má carry připravenou BKB.",
        "Support může před nocí umístit wardy kolem Roshanovy jámy a důležitých vstupů do jungle.",
        "Když se ti nedaří na lince, nehraj zbytečně agresivně. Získej bezpečně zkušenosti a farmu."
    )

    private var currentTip = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTip = savedInstanceState?.getInt(KEY_TIP_INDEX) ?: 0
        val tipText = view.findViewById<TextView>(R.id.tipText)
        val nextTipButton = view.findViewById<Button>(R.id.btnNextTip)

        fun showCurrentTip() {
            tipText.text = matchTips[currentTip]
        }

        showCurrentTip()
        nextTipButton.setOnClickListener {
            currentTip = (currentTip + 1) % matchTips.size
            showCurrentTip()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TIP_INDEX, currentTip)
    }

    companion object {
        private const val KEY_TIP_INDEX = "tip_index"
    }
}
