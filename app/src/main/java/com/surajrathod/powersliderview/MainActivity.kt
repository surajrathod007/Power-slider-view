package com.surajrathod.powersliderview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.surajrathod.powersliderview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener {
            binding.sampleView.startAnimation()
        }
    }

}