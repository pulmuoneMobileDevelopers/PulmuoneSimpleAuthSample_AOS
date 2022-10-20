package com.android.pulmuone.sample.ui.pin.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.pulmuone.sample.databinding.FragmentPincodeBinding
import com.android.pulmuone.sample.ui.pin.viewmodel.PinCodeViewModel
import com.android.pulmuone.sample.ui.pin.viewmodel.PinCodeViewModelFactory
import com.android.pulmuone.sample.ui.main.MainActivity
import com.android.pulmuone.sample.ui.base.Instance
import com.android.pulmuone.sample.utils.EncryptSharedPreferences

class PinCodeFragment : Fragment() {

    private lateinit var binding: FragmentPincodeBinding

    private lateinit var viewModel: PinCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPincodeBinding.inflate(inflater)
        val sharedPreferences =
            EncryptSharedPreferences.getInstance(requireContext()).sharedPreferences
        viewModel =
            ViewModelProvider(requireActivity(), PinCodeViewModelFactory(sharedPreferences))[PinCodeViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.pinViewModel = viewModel

        init()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("PinCodeFragment:::", "onDestroyView()")
    }

    private fun init() {
        context?.let { viewModel.initPinCodeTitle(it) }

        viewModel.shouldCloseLiveData.observe(requireActivity()) {
            requireActivity().overridePendingTransition(0, 0)
            requireActivity().finish()
        }

        viewModel.shouldMoveLiveData.observe(requireActivity()) {
            requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().overridePendingTransition(0, 0)
            requireActivity().finish()
        }
    }
    companion object : Instance<PinCodeFragment>(PinCodeFragment::class.java)
}