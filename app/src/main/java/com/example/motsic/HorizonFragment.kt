package com.example.motsic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.motsic.databinding.FragmentHorizonBinding

class HorizonFragment : Fragment() {

    private var _binding: FragmentHorizonBinding? = null
    private val binding get() = _binding!!

    private enum class Mode { CUES, LINE, DOT }
    private var mode: Mode = Mode.CUES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHorizonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.modeGroup.setOnCheckedStateChangeListener { _, ids ->
            val newMode = when (ids.firstOrNull()) {
                R.id.modeLine -> Mode.LINE
                R.id.modeDot -> Mode.DOT
                else -> Mode.CUES
            }
            if (newMode != mode) {
                mode = newMode
                applyMode()
            }
        }
        binding.btnPopOut.setOnClickListener { onPopOutClicked() }
    }

    private fun applyMode() {
        binding.motionCue.visibility = if (mode == Mode.CUES) View.VISIBLE else View.GONE
        binding.horizonView.visibility = if (mode == Mode.LINE) View.VISIBLE else View.GONE
        binding.focusDot.visibility = if (mode == Mode.DOT) View.VISIBLE else View.GONE
        binding.hint.visibility = if (mode == Mode.DOT) View.GONE else View.VISIBLE
        binding.hint.text = when (mode) {
            Mode.CUES -> getString(R.string.cues_hint)
            Mode.LINE -> getString(R.string.horizon_hint)
            Mode.DOT -> ""
        }
        when (mode) {
            Mode.CUES -> {
                binding.horizonView.stop()
                binding.motionCue.start()
            }
            Mode.LINE -> {
                binding.motionCue.stop()
                binding.horizonView.start()
            }
            Mode.DOT -> {
                binding.motionCue.stop()
                binding.horizonView.stop()
            }
        }
    }

    private fun onPopOutClicked() {
        val ctx = requireContext()
        if (OverlayService.isRunning(ctx)) {
            OverlayService.stop(ctx)
            updatePopOutLabel(false)
            return
        }
        if (!Settings.canDrawOverlays(ctx)) {
            Toast.makeText(ctx, R.string.overlay_permission_needed, Toast.LENGTH_LONG).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                )
            )
            return
        }
        OverlayService.start(ctx)
        updatePopOutLabel(true)
    }

    private fun updatePopOutLabel(running: Boolean) {
        binding.btnPopOut.text = getString(
            if (running) R.string.stop_overlay else R.string.pop_out
        )
    }

    override fun onResume() {
        super.onResume()
        applyMode()
        updatePopOutLabel(OverlayService.isRunning(requireContext()))
    }

    override fun onPause() {
        super.onPause()
        binding.motionCue.stop()
        binding.horizonView.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
