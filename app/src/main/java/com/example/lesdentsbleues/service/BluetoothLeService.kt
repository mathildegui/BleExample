package com.example.lesdentsbleues.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.lesdentsbleues.BleApplication

class BluetoothLeService : Service() {
    companion object {
        private const val TAG = "BluetoothLeService"
        const val ACTION_GATT_UPDATED = "com.example.lesdentsbleues.ACTION_GATT_UPDATED"
        const val ACTION_FETCH_SERVICES = "com.example.lesdentsbleues.ACTION_FETCH_SERVICES"
        const val ACTION_GATT_DISCONNECTED = "com.example.lesdentsbleues.ACTION_GATT_DISCONNECTED"
        const val ACTION_SERVICE_DISCOVERED = "com.example.lesdentsbleues.ACTION_SERVICE_DISCOVERED"
    }

    var bleGatt: BluetoothGatt? = null
        set(value) {
            field = value
            broadcastUpdate(ACTION_GATT_UPDATED)
        }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    /**
     * Connect to the ble device
     */
    fun connectDevice(device: BluetoothDevice?) {
        // update the status
        broadcastUpdate(ACTION_FETCH_SERVICES)
        bleGatt = device?.connectGatt(BleApplication.applicationContext(), false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            broadcastUpdate(ACTION_SERVICE_DISCOVERED)
            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer()
                return
            }
            bleGatt?.services?.forEach {
                Log.d(TAG, "onServicesDiscovered :: ${it.uuid}")
            }
            // log for successful discovery
            bleGatt = gatt
            Log.d(TAG, "Services discovery is successful")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            super.onConnectionStateChange(gatt, status, newState)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // update the connection status message
                Log.d(TAG, "Connected to the GATT server from $deviceAddress")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }
    }

    /**
     * Disconnect Gatt Server
     */
    fun disconnectGattServer() {
        Log.d(TAG, "Closing Gatt connection")
        // disconnect and close the gatt
        if (bleGatt != null) {
            bleGatt!!.disconnect()
            bleGatt!!.close()
        }
        broadcastUpdate(ACTION_GATT_DISCONNECTED)
    }

    private fun broadcastUpdate(action: String) = sendBroadcast(Intent(action))
}