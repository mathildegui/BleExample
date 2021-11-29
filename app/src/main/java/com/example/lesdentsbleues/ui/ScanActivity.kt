package com.example.lesdentsbleues.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lesdentsbleues.R
import com.example.lesdentsbleues.databinding.ActivityScanBinding
import com.example.lesdentsbleues.utils.extensions.commit
import com.example.lesdentsbleues.utils.extensions.hasPermission
import com.example.lesdentsbleues.utils.extensions.requestPermission
import com.example.lesdentsbleues.repo.BleRepository

class ScanActivity : AppCompatActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private lateinit var binding: ActivityScanBinding
    private lateinit var viewModel: ScanViewModel
    private val repository = BleRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ScanViewModel.get(this, application, repository)
        viewModel.setState(ScanViewModel.State.List)
        viewModel.registerBroadCastReceiver()

        registerObservers()
        requestLocationPermission()
    }

    private fun registerObservers() {
        viewModel.observeState(this) { state ->
            when (state) {
                ScanViewModel.State.List -> commit(
                    R.id.fragment_container_view,
                    DevicesFragment()
                )
                ScanViewModel.State.Detail -> commit(
                    R.id.fragment_container_view,
                    DeviceDetailsFragment()
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permission must be granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unregisterReceiver()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.disconnectDevice()
    }
}