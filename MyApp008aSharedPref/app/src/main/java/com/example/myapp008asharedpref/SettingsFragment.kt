package com.example.myapp008asharedpref

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp008asharedpref.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        binding.etNickname.setText(preferences.getString(KEY_NICKNAME, ""))
        binding.switchHardcoreMode.isChecked = preferences.getBoolean(KEY_HARDCORE, false)

        binding.btnSave.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            val hardcoreEnabled = binding.switchHardcoreMode.isChecked

            val editor = preferences.edit()
                .putString(KEY_NICKNAME, nickname)
                .putBoolean(KEY_HARDCORE, hardcoreEnabled)

            // Záměrně blokujeme UI thread, aby byl vidět problém starého přístupu.
            Thread.sleep(SIMULATED_WORK_MILLIS)
            editor.commit()

            Toast.makeText(
                requireContext(),
                R.string.shared_preferences_saved,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PREFERENCES_NAME = "GamerProfile"
        const val KEY_NICKNAME = "NICKNAME"
        const val KEY_HARDCORE = "HARDCORE"
        private const val SIMULATED_WORK_MILLIS = 3_000L
    }
}
