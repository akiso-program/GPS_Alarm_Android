package com.akiso.gps_alarm.placeholder

import android.support.v4.os.IResultReceiver
import com.akiso.gps_alarm.AlarmData
import com.google.android.gms.maps.model.LatLng
import java.time.LocalTime
import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<AlarmData> = ArrayList()
    val DEFAULT_DATA = AlarmData(
        0,
        LocalTime.of(1, 0),
        LocalTime.of(2, 0),
        listOf(Calendar.SUNDAY,Calendar.THURSDAY),
        LatLng(35.6817882863765, 139.76703598784582)
    )

    private const val COUNT = 3

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createAlarmData(i))
        }
    }
    fun getData(id: Int): AlarmData? {
        ITEMS.forEach{
            if(it.id == id)return it
        }
        return null
    }

    private fun addItem(item: AlarmData) {
        ITEMS.add(item)
    }

    private fun createAlarmData(position: Int): AlarmData {
        val locations = listOf(
            LatLng(35.6817882863765, 139.76703598784582),
            LatLng(34.73375038839483, 135.50033463624678),
            LatLng(33.59201931312273, 130.420219212164),
        )
        return AlarmData(
            position,
            LocalTime.of(1 + position, 0),
            LocalTime.of(2 + position, 0),
            listOf(Calendar.SUNDAY,Calendar.THURSDAY),
            locations[position % locations.size]
        )
    }


}