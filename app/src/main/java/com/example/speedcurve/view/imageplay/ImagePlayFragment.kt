package com.example.speedcurve.view.imageplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.speedcurve.databinding.FragmentImagePlayBinding
import com.example.speedcurve.util.getMediaFrameValue
import com.example.speedcurve.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImagePlayFragment : Fragment() {
    private var binding: FragmentImagePlayBinding? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagePlayBinding.inflate(layoutInflater, container, false)
        binding?.let {
            it.viewmodel = viewModel
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            mediaFrameValue.observe(viewLifecycleOwner) {
                binding?.imageView?.setImageResource(requireContext().getMediaFrameValue(it))
            }

            buttonName.observe(viewLifecycleOwner) {
                binding?.playPauseBtn?.text = it
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}

