package com.akiso.gps_alarm

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.akiso.gps_alarm.databinding.FragmentAlarmListItemBinding
import com.google.android.gms.maps.model.LatLng
import java.time.format.DateTimeFormatter

class MyItemRecyclerViewAdapter(
    private val values: List<AlarmData>,
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(FragmentAlarmListItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        (item.activeTimeStart.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + item.activeTimeEnd.format(DateTimeFormatter.ofPattern("HH:mm"))).also { holder.idView.text = it }
        holder.locateImage.setOnClickListener{ listener.onItemClick(item) }
    }

    override fun getItemCount(): Int = values.size

    private lateinit var listener: OnBookCellClickListener

    // 2. インターフェースを作成
    interface  OnBookCellClickListener {
        fun onItemClick(data: AlarmData)
    }

    // 3. リスナーをセット
    fun setOnBookCellClickListener(listener: OnBookCellClickListener) {
        // 定義した変数listenerに実行したい処理を引数で渡す（BookListFragmentで渡している）
        this.listener = listener
    }

    inner class ViewHolder(binding: FragmentAlarmListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val locateImage: ImageView = binding.locateImageView

        override fun toString(): String {
            return super.toString() + " '" + idView.text + "'"
        }
    }

}
