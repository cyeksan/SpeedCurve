package com.example.speedcurve.view.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.speedcurve.R
import com.example.speedcurve.databinding.ActivityMainBinding
import com.example.speedcurve.view.imageplay.ImagePlayFragment
import com.example.speedcurve.view.slider.SliderFragment
import com.example.speedcurve.view.speedcurve.SpeedCurveFragment
import com.example.speedcurve.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var playingJob: Job? = null
    private lateinit var speedCurveFragmentInstance: SpeedCurveFragment
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.toolbarHome.setNavigationOnClickListener {
            speedCurveFragmentInstance.validateAndSetEnteredValues()
        }
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<ImagePlayFragment>(R.id.home_fragment_container)
                add<SliderFragment>(R.id.slider_fragment_container)
            }
        }

        viewModel.let {
            it.isPlaying.observe(this) { _isPlaying ->
                playingJob?.cancel()

                if (_isPlaying) {
                    playingJob =
                        createNewPlayJob() // If current state is 'playing', new job is created
                }
            }

            // Replaces fragment according to speedCurvedEnabled value
            it.speedCurveEnabled.observe(this) { _isSpeedCurveEnabled ->
                if (_isSpeedCurveEnabled) {
                    speedCurveFragmentInstance = SpeedCurveFragment.newInstance()
                    replaceWith(speedCurveFragmentInstance)
                } else {
                    replaceWith(SliderFragment.newInstance())

                }
            }
        }
    }

    private fun replaceWith(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.slider_fragment_container, fragment)
        }

        when(fragment) {
            is SpeedCurveFragment -> {
                binding.toolbarHome.visibility = View.VISIBLE
                viewModel.setIsNotFirstLaunch()
                viewModel.setIsSpeedCurveValuesInRange(false)
            }

            is SliderFragment -> {
                binding.toolbarHome.visibility = View.INVISIBLE
                viewModel.generateProjectAndMediaFrames(
                    viewModel.index.value!!, viewModel.speed1.value!!,
                    viewModel.speed2.value!!, viewModel.speed3.value!!
                )
                viewModel.startPlaying()
            }
        }
    }

    private fun createNewPlayJob(): Job {

        return lifecycleScope.launch {
            while (isActive) {
                viewModel.collectImages(
                    viewModel.projectFrameValue.value!!
                )
            }
        }
    }
}