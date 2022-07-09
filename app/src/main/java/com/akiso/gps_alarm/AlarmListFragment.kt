package com.akiso.gps_alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akiso.gps_alarm.placeholder.PlaceholderContent
import java.time.LocalTime
import java.util.*


/**
 * A fragment representing a list of Items.
 */
class AlarmListFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val myAdapter = MyItemRecyclerViewAdapter(PlaceholderContent.ITEMS)
                myAdapter.setOnBookCellClickListener(
                    object : MyItemRecyclerViewAdapter.OnCellClickListener {
                        override fun onLocationImageClick(data: AlarmData) {
                            val params = bundleOf("AlarmID" to data.id )
                            // 画面遷移処理
                            findNavController().navigate(R.id.action_alarmListFragment_to_mapFragment, params)
                        }

                        override fun onStartTimeClick(data: AlarmData,position: Int) {
                            val hour = data.activeTimeStart.hour
                            val minute = data.activeTimeStart.minute
                            val dialog = TimePickerDialog(
                                requireContext(),
                                { _, newHour, newMinute ->
                                    data.activeTimeStart = LocalTime.of(newHour,newMinute)
                                    myAdapter.notifyItemChanged(position)
                                },
                                hour,minute,true)
                            dialog.show()
                        }

                        override fun onEndTimeClick(data: AlarmData,position: Int) {
                            val hour = data.activeTimeEnd.hour
                            val minute = data.activeTimeEnd.minute
                            val dialog = TimePickerDialog(
                                requireContext(),
                                { _, newHour, newMinute ->
                                    data.activeTimeEnd = LocalTime.of(newHour,newMinute)
                                    myAdapter.notifyItemChanged(position)
                                },
                                hour,minute,true)
                            dialog.show()
                        }

                        override fun onDayClick(data: AlarmData, index: Int) {
                            if(data.activeDay.contains(index)){
                                data.activeDay.remove(index)
                            }else{
                                data.activeDay.add(index)
                            }
                        }

                        override fun onAddButtonClick(data: AlarmData) {
                            val params = bundleOf("AlarmID" to data.id )
                            PlaceholderContent.makeData()
                            myAdapter.notifyItemRangeChanged(data.id,2)
                            // 画面遷移処理
                            findNavController().navigate(R.id.action_alarmListFragment_to_mapFragment, params)
                        }
                    }
                )
                adapter = myAdapter
            }
        }
        setAlarmService()

        return view
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setAlarmService(){
        val nextStart = PlaceholderContent.getNextStart(LocalTime.now())
        nextStart?.also {
            val serviceIntent = Intent(activity, GpsAlarmStartService::class.java)
            val pendingIntent = PendingIntent.getActivity(requireContext(),0,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            val calendar: Calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, nextStart.hour)
                set(Calendar.MINUTE, nextStart.minute)
                set(Calendar.SECOND, 0) //0秒
                set(Calendar.MILLISECOND, 0) //カンマ0秒
            }
            val alarm = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager?
            alarm?.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, pendingIntent)
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AlarmListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}