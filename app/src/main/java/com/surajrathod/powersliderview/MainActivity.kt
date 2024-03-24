package com.surajrathod.powersliderview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.surajrathod.powersliderview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    private fun setupListeners() {
        binding.powerView.setOnCheckChangedListener(object : PowerSliderView.OnCheckChangedListener {
            override fun onCheckChanged(state: Int) {
                when(state){
                    PowerSliderView.STATE_IDLE ->{
                        binding.txtLblAns.text = "Idle state"
                    }

                    PowerSliderView.STATE_TOP_SELECTED ->{
                        binding.txtLblAns.text = "Top selected"
                    }

                    PowerSliderView.STATE_BOTTOM_SELECTED ->{
                        binding.txtLblAns.text = "Bottom selected"
                    }
                }
            }
        })
    }


}