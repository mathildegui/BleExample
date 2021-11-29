package com.example.lesdentsbleues.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.lesdentsbleues.BleApplication
import com.example.lesdentsbleues.repo.BleRepository
import com.example.lesdentsbleues.utils.extensions.cast
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class ScanViewModel(application: Application, private val repository: BleRepository) :
    AndroidViewModel(application) {
    class Factory(private val application: Application, private val repository: BleRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ScanViewModel(application = application, repository = repository).cast()
    }

    companion object {
        const val TAG = "ScanViewModel"
        const val SCAN_PERIOD: Long = 10000  // Stops scanning after 10 seconds.
        fun get(
            owner: ViewModelStoreOwner,
            application: Application,
            repository: BleRepository
        ): ScanViewModel =
            ViewModelProvider(owner, Factory(application, repository)).get()
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            BleApplication.applicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    enum class State {
        List, Detail
    }

//    val mainHandler = Handler(Looper.getMainLooper())

    // Values
    private val state: MutableLiveData<State> = MutableLiveData()
    private val device: MutableLiveData<BluetoothDevice> = MutableLiveData(null)
    private val isScanning: MutableLiveData<Boolean> = MutableLiveData(false)
    private val scanResult: MutableLiveData<ScanResult> = MutableLiveData()

    private val scanResults = mutableListOf<ScanResult>()
    private val _scanResults: MutableLiveData<List<ScanResult>> = MutableLiveData()


    private val _gattServices: LiveData<BluetoothGatt>
        get() = repository.gatt

    private val _fetching: LiveData<Boolean>
        get() = repository.fetching

    // Observers
    fun observeState(owner: LifecycleOwner, observer: (State) -> Unit) {
        viewModelScope.launch {
            state.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeDevice(owner: LifecycleOwner, observer: (BluetoothDevice) -> Unit) {
        viewModelScope.launch {
            device.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeIsScanning(owner: LifecycleOwner, observer: (Boolean) -> Unit) {
        viewModelScope.launch {
            isScanning.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeScanResult(owner: LifecycleOwner, observer: (ScanResult) -> Unit) {
        viewModelScope.launch {
            scanResult.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeScanResults(owner: LifecycleOwner, observer: (List<ScanResult>) -> Unit) {
        viewModelScope.launch {
            _scanResults.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeGatt(owner: LifecycleOwner, observer: (BluetoothGatt?) -> Unit) {
        viewModelScope.launch {
            _gattServices.observe(owner::getLifecycle) { observer(it) }
        }
    }

    fun observeFetching(owner: LifecycleOwner, observer: (Boolean) -> Unit) {
        viewModelScope.launch {
            _fetching.observe(owner::getLifecycle) { observer(it) }
        }
    }

    // Setters
    private fun setIsScanning(isScanning: Boolean) {
        this.isScanning.postValue(isScanning)
    }

    fun addScanResult(scanResult: ScanResult) {
        this.scanResults.add(scanResult)
        this.scanResult.postValue(scanResult)
    }

    fun initDetail(device: BluetoothDevice) {
        stopScan()
        setDevice(device)
        connectGattDevice(device)
        setState(State.Detail)
    }

    fun setState(state: State) {
        this.state.value = state
    }

    private fun setDevice(device: BluetoothDevice) {
        this.device.value = device
    }

    // Functions
    fun scan() {
        when (isScanning.value) {
            true -> {
                stopScan()
            }
            false -> {
                startScan()
//                startListenRssi()
                Timer(TAG, false).schedule(SCAN_PERIOD) { stopScan() }
            }
            else -> {
                // Do nothing
            }
        }
    }

    /*private fun startListenRssi() {
        mainHandler.post(object : Runnable {
            override fun run() {
                _scanResults.postValue(scanResults)
                mainHandler.postDelayed(this, SCAN_PERIOD)
            }
        })
    }*/

    private fun startScan() {
        setIsScanning(true)
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    private fun stopScan() {
        setIsScanning(false)
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    fun bindService() {
        repository.bindService()
        Log.d(TAG, "bindService")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i(
                    TAG,
                    "ScanCallback: Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                )
            }
            addScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "ScanCallback: onScanFailed: code $errorCode")
            // Display error here
        }
    }

    fun disconnectDevice() {
        repository.disconnectDevice()
    }

    private fun connectGattDevice(device: BluetoothDevice) {
        repository.connectDevice(device)
    }

    fun registerBroadCastReceiver() {
        repository.registerGattReceiver()
    }

    fun unregisterReceiver() {
        repository.unregisterReceiver()
    }
}