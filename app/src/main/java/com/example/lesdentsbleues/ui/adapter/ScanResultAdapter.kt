package com.example.lesdentsbleues.ui.adapter

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.lesdentsbleues.R

@SuppressLint("NotifyDataSetChanged")
class ScanResultAdapter(
    private val dataSet: MutableList<ScanResult> = mutableListOf<ScanResult>(),
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_scan_result, parent, false)

        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = dataSet.size

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.bind(item)
    }

    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {
        private val deviceName: TextView = view.findViewById(R.id.device_name)
        private val macAddress: TextView = view.findViewById(R.id.mac_address)
        private val signalStrength: TextView = view.findViewById(R.id.signal_strength)

        @RequiresApi(Build.VERSION_CODES.R)
        fun bind(result: ScanResult) {
            deviceName.text = result.device.name ?: result.device.alias ?: "Unknown"
            macAddress.text = result.device.address
            signalStrength.text = "${result.rssi} dBm"
            view.setOnClickListener { onClickListener.invoke(result) }
            view.isEnabled = result.isConnectable
            view.setBackgroundResource(if (result.isConnectable) R.color.green_300 else R.color.red_300)
        }
    }

    fun addItem(item: ScanResult) {
        val indexQuery = dataSet.indexOfFirst { it.device.address == item.device.address }
        if (indexQuery != -1) {
            dataSet[indexQuery] = item
        } else {
            dataSet.add(item)
        }
        dataSet.sortBy { it.rssi }
        notifyDataSetChanged()
    }

    fun updateItems(items: List<ScanResult>) {
        items.forEach { addItem(it) }
    }

    fun reset() {
        dataSet.clear()
        notifyDataSetChanged()
    }
}