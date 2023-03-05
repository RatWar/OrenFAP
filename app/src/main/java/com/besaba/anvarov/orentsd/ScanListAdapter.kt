package com.besaba.anvarov.orentsd

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.besaba.anvarov.orentsd.room.CountData

class ScanListAdapter internal constructor(context: Context) : RecyclerView.Adapter<ScanListAdapter.ScanViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var scans = emptyList<CountData>() // Cached copy of scans

    private var onScanClickListener: OnScanClickListener? = null

    interface OnScanClickListener {
        fun onScanClick(scan: CountData, del: Boolean)
    }

    fun scanAdapter(onScanClickListener: OnScanClickListener) {
        this.onScanClickListener = onScanClickListener
    }

    inner class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val scanItemBarcode: TextView = itemView.findViewById(R.id.codeBarcode)
        val scanItemDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
        val scanItemName: TextView = itemView.findViewById(R.id.nameBarcode)
        val scanItemEI: TextView = itemView.findViewById(R.id.cntBarcode)
    }

    internal fun setScans(scanData: List<CountData>) {
        this.scans = scanData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val itemView = inflater.inflate(R.layout.scanlist_view_item, parent, false)
        return ScanViewHolder(itemView)
    }

    override fun getItemCount() = scans.size

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val current = scans[position]
        holder.scanItemBarcode.text = current.barcode
        holder.scanItemName.text = current.nameNomen
        holder.scanItemEI.text = current.cnt.toString()

        holder.scanItemBarcode.setOnClickListener {
            val scan = scans[position]
            onScanClickListener?.onScanClick(scan, false)
        }

        holder.scanItemDelete.setOnClickListener {
            val scan = scans[position]
            onScanClickListener?.onScanClick(scan, true)
        }

        holder.scanItemName.setOnClickListener {
            val scan = scans[position]
            onScanClickListener?.onScanClick(scan, false)
        }

        holder.scanItemEI.setOnClickListener {
            val scan = scans[position]
            onScanClickListener?.onScanClick(scan, false)
        }
    }
}