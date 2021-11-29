package com.example.lesdentsbleues.ui

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lesdentsbleues.databinding.DeviceDetailsFragmentBinding
import com.example.lesdentsbleues.ui.adapter.GattAdapter

class DeviceDetailsFragment : Fragment() {
    private var _binding: DeviceDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels()
    private val gattAdapter: GattAdapter = GattAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeviceDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        registerObservers()
    }

    private fun registerObservers() {
        viewModel.observeDevice(this) {
            bindDevice(it)
        }
        viewModel.observeGatt(this) {
            gattAdapter.updateDataSet(it)
        }
        viewModel.observeFetching(this) { fetching ->
            binding.fetchingDate.visibility = if (fetching) View.VISIBLE else View.GONE
        }
    }

    private fun bindDevice(device: BluetoothDevice) {
        binding.name.text = device.name
        binding.address.text = device.address
    }

    private fun setupRecyclerView() {
        binding.servicesRv.apply {
            adapter = gattAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }
    }
}