package com.example.motsic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.motsic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            show(HorizonFragment())
            binding.bottomNav.selectedItemId = R.id.nav_horizon
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val frag: Fragment = when (item.itemId) {
                R.id.nav_horizon -> HorizonFragment()
                R.id.nav_breathe -> BreatheFragment()
                R.id.nav_acupressure -> AcupressureFragment()
                R.id.nav_log -> LogFragment()
                else -> return@setOnItemSelectedListener false
            }
            show(frag)
            true
        }
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit()
    }
}
