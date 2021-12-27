package com.example.speedcurve.view.speedcurve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.speedcurve.R
import com.example.speedcurve.databinding.FragmentSpeedCurveBinding
import com.example.speedcurve.util.toast
import com.example.speedcurve.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpeedCurveFragment : Fragment() {
    private var binding: FragmentSpeedCurveBinding? = null

    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpeedCurveBinding.inflate(layoutInflater, container, false)
        binding?.viewmodel = viewModel
        return binding?.root
    }

    companion object {
        fun newInstance() = SpeedCurveFragment()
    }

    fun validateAndSetEnteredValues() {
        val result: Boolean


        viewModel.apply {
            result = validateEditTextValue(
                binding?.speed1Edt?.text.toString(),
                binding?.speed2Edt?.text.toString(),
                binding?.speed3Edt?.text.toString(),
                binding?.index2Edt?.text.toString()
            ) {
                // If validation is successful, speed and index values are set:
                setSpeedAndIndexValues(
                    binding?.speed1Edt?.text.toString().toFloat(),
                    binding?.speed2Edt?.text.toString().toFloat(),
                    binding?.speed3Edt?.text.toString().toFloat(),
                    binding?.index2Edt?.text.toString().toInt()
                )
                setCurrentProjectFrameValue(0)
                setIsSpeedCurveValuesInRange(true)
            }

            // SpeedCurveFragment is disabled regardless of the validation result
            enableSpeedCurveFragment(false)

        }

        // If validation is not successful, a toast message is shown
        if (!result) requireContext().toast(getString(R.string.speed_curve_input_error))
    }

    override fun onDetach() {
        super.onDetach()
        binding = null

    }
}