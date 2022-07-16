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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalTime
import java.util.*


/**
 * A fragment representing a list of Items.
 */
class AlarmListFragment : Fragment() {

    private var columnCount = 1
    private val myModel = ViewModelProvider(this)[MyViewModel::class.java]

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

        var alarmData:List<AlarmData> = listOf()
        myModel.data.observe(viewLifecycleOwner) { alarmData = it }
        myModel.getAll()

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val myAdapter = MyItemRecyclerViewAdapter(alarmData)
                myAdapter.setOnBookCellClickListener(
                    object : MyItemRecyclerViewAdapter.OnCellClickListener {
                        override fun onLocationImageClick(data: AlarmData) {
                            val params = bundleOf("AlarmID" to data.id )
                            // 画面遷移処理
                            findNavController().navigate(R.id.action_alarmListFragment_to_mapFragment, params)
                        }

                        override fun onStartTimeClick(data: AlarmData,position: Int) {
                            val calender = data.startToCalendar()
                            val hour = calender.get(Calendar.HOUR_OF_DAY)
                            val minute = calender.get(Calendar.MINUTE)
                            val dialog = TimePickerDialog(
                                requireContext(),
                                { _, newHour, newMinute ->
                                    data.activeTimeStart = java.sql.Time.valueOf(LocalTime.of(newHour,newMinute).toString())
                                    myAdapter.notifyItemChanged(position)
                                },
                                hour,minute,true)
                            dialog.show()
                        }

                        override fun onEndTimeClick(data: AlarmData,position: Int) {
                            val calender = data.endToCalendar()
                            val hour = calender.get(Calendar.HOUR_OF_DAY)
                            val minute = calender.get(Calendar.MINUTE)
                            val dialog = TimePickerDialog(
                                requireContext(),
                                { _, newHour, newMinute ->
                                    data.activeTimeEnd = java.sql.Time.valueOf(LocalTime.of(newHour,newMinute).toString())
                                    myAdapter.notifyItemChanged(position)
                                },
                                hour,minute,true)
                            dialog.show()
                        }

                        override fun onDayClick(data: AlarmData, index: Int) {
                            when (index){
                                Calendar.SUNDAY -> data.activeOnSunday = !data.activeOnSunday
                                Calendar.MONDAY -> data.activeOnMonday = !data.activeOnMonday
                                Calendar.TUESDAY -> data.activeOnTuesday = !data.activeOnTuesday
                                Calendar.WEDNESDAY -> data.activeOnWednesday = !data.activeOnWednesday
                                Calendar.THURSDAY -> data.activeOnThursday = !data.activeOnThursday
                                Calendar.FRIDAY -> data.activeOnFriday = !data.activeOnFriday
                                Calendar.SATURDAY -> data.activeOnSaturday = !data.activeOnSaturday
                            }
                        }

                        override fun onAddButtonClick(data: AlarmData) {
                            val params = bundleOf("AlarmID" to data.id )
                            myModel.newData()
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
        val nextStart = myModel.getNextStart(Calendar.getInstance())
        nextStart?.also {
            val serviceIntent = Intent(activity, GpsAlarmStartService::class.java)
            val pendingIntent = PendingIntent.getActivity(requireContext(),0,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            val calendar: Calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, nextStart.startToCalendar().get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, nextStart.startToCalendar().get(Calendar.MINUTE))
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