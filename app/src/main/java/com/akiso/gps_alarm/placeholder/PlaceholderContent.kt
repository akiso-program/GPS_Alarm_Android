package com.akiso.gps_alarm.placeholder

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

    private const val COUNT = 3

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createAlarmData(i))
        }
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