package com.example.myapp008bdatastore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapp008bdatastore.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        viewLifecycleOwner.lifecycleScope.launch {
            showProfile(requireContext().playerProfile.first())
        }

        binding.responsivenessSlider.addOnChangeListener { _, value, _ ->
            binding.tvSliderValue.text = getString(
                R.string.interactive_test_value,
                value.toInt()
            )
        }

        binding.btnSave.setOnClickListener {
            val newProfile = PlayerProfile(
                nickname = binding.etNickname.text.toString(),
                hardcoreEnabled = binding.switchHardcoreMode.isChecked
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val previousProfile = requireContext().playerProfile.first()
                setSavingState(isSaving = true)

                try {
                    // Na rozdíl od Thread.sleep neblokuje delay hlavní UI vlákno.
                    delay(SIMULATED_WORK_MILLIS)
                    requireContext().savePlayerProfile(newProfile)
                    showSavedSnackbar(previousProfile)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Snackbar.make(
                        binding.root,
                        R.string.datastore_save_failed,
                        Snackbar.LENGTH_LONG
                    ).show()
                } finally {
                    setSavingState(isSaving = false)
                }
            }
        }
    }

    private fun showProfile(profile: PlayerProfile) {
        binding.etNickname.setText(profile.nickname)
        binding.switchHardcoreMode.isChecked = profile.hardcoreEnabled
    }

    private fun setSavingState(isSaving: Boolean) {
        binding.progressBar.visibility = if (isSaving) View.VISIBLE else View.INVISIBLE
        binding.tvSaving.visibility = if (isSaving) View.VISIBLE else View.INVISIBLE
        binding.btnSave.isEnabled = !isSaving
    }

    private fun showSavedSnackbar(previousProfile: PlayerProfile) {
        Snackbar.make(binding.root, R.string.datastore_saved, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                viewLifecycleOwner.lifecycleScope.launch {
                    requireContext().savePlayerProfile(previousProfile)
                    showProfile(previousProfile)
                    Snackbar.make(
                        binding.root,
                        R.string.undo_finished,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SIMULATED_WORK_MILLIS = 3_000L
    }
}
