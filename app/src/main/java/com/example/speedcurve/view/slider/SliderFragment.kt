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
    private lateinit var projectAndMediaFrames: HashMap<Int, Int>

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
                viewModel.updateCurrentMediaFrameWithSliderPosition(value.toInt())
            }
        }

        return binding?.root
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            projectAndMediaFrames = generateProjectAndMediaFrames(
                viewModel.index.value!!, viewModel.speed1.value!!,
                viewModel.speed2.value!!, viewModel.speed3.value!!
            )
            setSliderMaxValue(projectAndMediaFrames.size - 1)
        }

    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}