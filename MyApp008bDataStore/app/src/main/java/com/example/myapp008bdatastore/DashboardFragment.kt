package com.example.myapp008bdatastore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapp008bdatastore.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            requireContext().playerProfile.collect { profile ->
                binding.tvNickname.text = if (profile.nickname.isBlank()) {
                    getString(R.string.dashboard_nickname_empty)
                } else {
                    getString(R.string.dashboard_nickname, profile.nickname)
                }
                binding.tvHardcore.text = getString(
                    if (profile.hardcoreEnabled) {
                        R.string.dashboard_hardcore_enabled
                    } else {
                        R.string.dashboard_hardcore_disabled
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
