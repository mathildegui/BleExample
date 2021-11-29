package com.example.lesdentsbleues.ui.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lesdentsbleues.model.CustomGatt

class GattAdapter :
    RecyclerView.Adapter<GattAdapter.ViewHolder>() {
    private var dataset = listOf<CustomGatt>()

    class ViewHolder(
        private val view: TextView
    ) : RecyclerView.ViewHolder(view) {
        fun bind(gatt: CustomGatt) {
            view.text = gatt.type.toString().plus(" : ").plus(gatt.uuid)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.bind(item)
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(gatt: BluetoothGatt?) {
        this.dataset = bindGatt(gatt)
        notifyDataSetChanged()
    }

    private fun bindGatt(gatt: BluetoothGatt?): List<CustomGatt> {
        val gattList = arrayListOf<CustomGatt>()
        gatt?.services?.forEach { service ->
            gattList.add(CustomGatt(service.uuid.toString(), CustomGatt.Type.SERVICE))
            service.characteristics.forEach { char ->
                gattList.add(CustomGatt(char.uuid.toString(), CustomGatt.Type.CHAR))
            }
        }
        return gattList
    }
}