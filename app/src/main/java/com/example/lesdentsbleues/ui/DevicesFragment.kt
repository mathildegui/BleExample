package com.example.lesdentsbleues.ui

import android.Manifest
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.lesdentsbleues.R
import com.example.lesdentsbleues.databinding.DevicesFragmentBinding
import com.example.lesdentsbleues.ui.adapter.ScanResultAdapter
import com.example.lesdentsbleues.utils.extensions.hasPermission

class DevicesFragment : Fragment() {

    private var _binding: DevicesFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels()

    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter {
            // The user clicked on a device
            viewModel.initDetail(it.device)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DevicesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scanBle.setOnClickListener {
            viewModel.scan()
        }
        setupRecyclerView()
        registerObservers()
        viewModel.bindService()
    }

    override fun onResume() {
        super.onResume()
        binding.scanBle.isEnabled =
            requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun registerObservers() {
        viewModel.observeIsScanning(this) { isScanning ->
            when (isScanning) {
                true -> {
                    scanResultAdapter.reset()
                    binding.scanBle.text = getString(R.string.scanning)
                    binding.scanBle.isEnabled = false
                }
                false -> {
                    binding.scanBle.text = getString(R.string.start_scan)
                    binding.scanBle.isEnabled = true
                }
            }
        }

        viewModel.observeScanResult(this) { result ->
            scanResultAdapter.addItem(result)
        }

        viewModel.observeScanResults(this) { results ->
            scanResultAdapter.updateItems(results)
        }
    }

    private fun setupRecyclerView() {
        binding.scanResultsRecyclerView.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = binding.scanResultsRecyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}