package com.example.lesdentsbleues.repo

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.lesdentsbleues.BleApplication
import com.example.lesdentsbleues.service.BluetoothLeService
import com.example.lesdentsbleues.service.BluetoothLeService.Companion.ACTION_FETCH_SERVICES
import com.example.lesdentsbleues.service.BluetoothLeService.Companion.ACTION_GATT_DISCONNECTED
import com.example.lesdentsbleues.service.BluetoothLeService.Companion.ACTION_GATT_UPDATED
import com.example.lesdentsbleues.service.BluetoothLeService.Companion.ACTION_SERVICE_DISCOVERED

class BleRepository {
    companion object {
        const val TAG = "BleRepository"
    }

    var fetching = MutableLiveData(false)
    var bleService: BluetoothLeService? = null
    var gatt = MutableLiveData<BluetoothGatt>(null)

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            binder: IBinder
        ) {
            val bluetoothService = (binder as BluetoothLeService.LocalBinder).getService()
            bluetoothService.let {
                bleService = it
                Log.d(TAG, "ServiceConnection: on service connected.")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bleService = null
            Log.d(TAG, "ServiceConnection: disconnected from service.")
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        bleService?.connectDevice(device)
    }

    fun disconnectDevice() {
        bleService?.disconnectGattServer()
    }

    fun bindService() {
        Intent(BleApplication.applicationContext(), BluetoothLeService::class.java).also { intent ->
            BleApplication.applicationContext().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    /**
     * Handles various events fired by the Service.
     */
    private var mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "action ${intent.action}")
            when (intent.action) {
                ACTION_GATT_UPDATED -> {
                    gatt.postValue(bleService?.bleGatt)
                }
                ACTION_FETCH_SERVICES -> {
                    fetching.postValue(true)
                }
                ACTION_GATT_DISCONNECTED, ACTION_SERVICE_DISCOVERED -> {
                    fetching.postValue(false)
                }
            }
        }
    }

    fun registerGattReceiver() {
        BleApplication.applicationContext().registerReceiver(
            mGattUpdateReceiver,
            makeGattUpdateIntentFilter()
        )
    }

    fun unregisterReceiver() {
        BleApplication.applicationContext().unregisterReceiver(mGattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_GATT_UPDATED)
        intentFilter.addAction(ACTION_SERVICE_DISCOVERED)
        intentFilter.addAction(ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(ACTION_FETCH_SERVICES)
        return intentFilter
    }
}