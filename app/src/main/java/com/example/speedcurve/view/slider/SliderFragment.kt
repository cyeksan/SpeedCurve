package com.example.speedcurve.view.slider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.speedcurve.databinding.FragmentSliderBinding
import com.example.speedcurve.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi

@AndroidEntryPoint
class SliderFragment : Fragment() {
    private var binding: FragmentSliderBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var mediaFramesArray : IntArray
    private lateinit var projectFramesArray : IntArray

    companion object {
        fun newInstance() = SliderFragment()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSliderBinding.inflate(layoutInflater, container, false)
        binding?.let {

            it.viewmodel = viewModel
            it.lifecycleOwner = this
            it.slider.addOnChangeListener { _, value, _ ->
                viewModel.changeSliderPosition(value.toInt())
            }
            it.speedCurveBtn.setOnClickListener {
                viewModel.enableSpeedCurveFragment(true)
            }
        }

        return binding?.root
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaFramesArray = viewModel.getMediaFrames(
            viewModel.index.value!!, viewModel.speed1.value!!,
            viewModel.speed2.value!!, viewModel.speed3.value!!)

        projectFramesArray = viewModel.getProjectFrames(mediaFramesArray)

        viewModel.apply {
            setMediaFrames(mediaFramesArray)
            setProjectFrames(projectFramesArray)
            setSliderMaxValue(projectFramesArray.size - 1)

            sliderEnabled.observe(viewLifecycleOwner) {
                binding?.slider?.isEnabled = it
            }

            sliderMaxValue.observe(viewLifecycleOwner) {
                binding?.slider?.valueTo = it?.toFloat()!!
            }

            sliderPosition.observe(viewLifecycleOwner) {
                viewModel.setStartPositionValue(it)
            }


        }

    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}