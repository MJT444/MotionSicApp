package com.example.motsic

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.motsic.databinding.FragmentAcupressureBinding

class AcupressureFragment : Fragment() {

    private var _binding: FragmentAcupressureBinding? = null
    private val binding get() = _binding!!
    private var timer: CountDownTimer? = null
    private val totalMs = 2 * 60 * 1000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcupressureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateLabel(totalMs)
        binding.btnStart.setOnClickListener {
            if (timer == null) startTimer() else stopTimer()
        }
    }

    private fun startTimer() {
        binding.btnStart.text = getString(R.string.stop)
        timer = object : CountDownTimer(totalMs, 200) {
            override fun onTick(ms: Long) = updateLabel(ms)
            override fun onFinish() {
                updateLabel(0)
                vibrate()
                timer = null
                binding.btnStart.text = getString(R.string.start_2min)
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        binding.btnStart.text = getString(R.string.start_2min)
        updateLabel(totalMs)
    }

    private fun updateLabel(ms: Long) {
        val totalSeconds = (ms / 1000).toInt()
        binding.timer.text = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    private fun vibrate() {
        val ctx = context ?: return
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
