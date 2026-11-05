package com.example.myapp008asharedpref

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapp008asharedpref.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val preferences = requireContext().getSharedPreferences(
            SettingsFragment.PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        val nickname = preferences.getString(SettingsFragment.KEY_NICKNAME, "").orEmpty()
        val hardcoreEnabled = preferences.getBoolean(SettingsFragment.KEY_HARDCORE, false)

        binding.tvNickname.text = if (nickname.isBlank()) {
            getString(R.string.dashboard_nickname_empty)
        } else {
            getString(R.string.dashboard_nickname, nickname)
        }
        binding.tvHardcore.text = getString(
            if (hardcoreEnabled) {
                R.string.dashboard_hardcore_enabled
            } else {
                R.string.dashboard_hardcore_disabled
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
