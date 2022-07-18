package com.akiso.gps_alarm

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.akiso.gps_alarm.databinding.FragmentAlarmListItemBinding
import java.text.SimpleDateFormat
import java.util.*

class MyItemRecyclerViewAdapter(
    private val values: List<AlarmData>,
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(FragmentAlarmListItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(itemCount-1 == position){
            holder.addButton.visibility = if (itemCount - 1 == position) View.VISIBLE else View.GONE
            holder.addButton.setOnClickListener { listener.onAddButtonClick() }
        }else {
            val item = values[position]
            holder.startTimeText.text =
                SimpleDateFormat("HH:mm", Locale.JAPANESE).format(item.startToCalendar().time)
            holder.endTimeText.text =
                SimpleDateFormat("HH:mm", Locale.JAPANESE).format(item.endToCalendar().time)
            holder.locateImage.setOnClickListener {
                listener.onLocationImageClick(item)
                notifyItemChanged(position)
            }
            holder.startTimeText.setOnClickListener {
                listener.onStartTimeClick(item,
                    position); notifyItemChanged(position)
            }
            holder.endTimeText.setOnClickListener {
                listener.onEndTimeClick(item,
                    position); notifyItemChanged(position)
            }
            holder.locateImage.setOnClickListener {
                listener.onLocationImageClick(item); notifyItemChanged(position)
            }
            holder.locateImage.setOnClickListener {
                listener.onLocationImageClick(item); notifyItemChanged(position)
            }
            holder.daysTexts.forEachIndexed { index, textView ->
                fun changeButton(active: Boolean) {
                    if (active) {
                        textView.apply {
                            setBackgroundColor(Color.BLUE)
                            setTextColor(Color.WHITE)
                        }
                    } else {
                        textView.apply {
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                        }
                    }
                }
                when (index) {
                    Calendar.SUNDAY -> {
                        changeButton(item.activeOnSunday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.MONDAY -> {
                        changeButton(item.activeOnMonday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.TUESDAY -> {
                        changeButton(item.activeOnTuesday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.WEDNESDAY -> {
                        changeButton(item.activeOnWednesday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.THURSDAY -> {
                        changeButton(item.activeOnThursday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.FRIDAY -> {
                        changeButton(item.activeOnFriday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                    Calendar.SATURDAY -> {
                        changeButton(item.activeOnSaturday)
                        textView.setOnClickListener {
                            listener.onDayClick(item,
                                index + 1); notifyItemChanged(position)
                        }
                    }
                }
            }
            holder.addButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = values.size + 1

    private lateinit var listener: OnCellClickListener

    interface  OnCellClickListener {
        fun onLocationImageClick(data: AlarmData)
        fun onStartTimeClick(data: AlarmData, position:Int)
        fun onEndTimeClick(data: AlarmData,position:Int)
        fun onDayClick(data: AlarmData, index:Int)
        fun onAddButtonClick()
    }

    fun setOnBookCellClickListener(listener: OnCellClickListener) {
        // 定義した変数listenerに実行したい処理を引数で渡す（BookListFragmentで渡している）
        this.listener = listener
    }

    inner class ViewHolder(binding: FragmentAlarmListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val addButton = binding.addButton
        val startTimeText: TextView = binding.startTimeText
        val endTimeText:TextView = binding.endTimeText
        val locateImage: ImageView = binding.locateImageView
        private val sundayText = binding.textSunday
        private val mondayText = binding.textMonday
        private val tuesdayText = binding.textTuesday
        private val wednesdayText = binding.textWednesday
        private val thursText = binding.textThursday
        private val fridayText = binding.textFriday
        private val saturdayText = binding.textSaturday
        val daysTexts = listOf(sundayText,mondayText,tuesdayText,wednesdayText,thursText,fridayText,saturdayText)

        override fun toString(): String {
            return super.toString() + " '" + startTimeText.text + "'"
        }
    }

}
