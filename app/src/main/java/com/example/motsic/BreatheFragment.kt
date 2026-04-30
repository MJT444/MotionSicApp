package com.example.motsic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.motsic.databinding.FragmentBreatheBinding

class BreatheFragment : Fragment() {

    private var _binding: FragmentBreatheBinding? = null
    private val binding get() = _binding!!
    private var running = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreatheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener {
            if (running) stopBreathing() else startBreathing()
        }
    }

    private fun startBreathing() {
        running = true
        binding.breatheView.start()
        binding.btnStart.text = getString(R.string.stop)
    }

    private fun stopBreathing() {
        running = false
        binding.breatheView.stop()
        binding.btnStart.text = getString(R.string.start)
    }

    override fun onPause() {
        super.onPause()
        if (running) stopBreathing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
