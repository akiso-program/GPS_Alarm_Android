package com.akiso.gps_alarm.placeholder

import android.support.v4.os.IResultReceiver
import com.akiso.gps_alarm.AlarmData
import com.google.android.gms.maps.model.LatLng
import java.sql.Time
import java.time.LocalDate
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
        Time.valueOf("01:00:00"),
        Time.valueOf("02:00:00"),
        activeOnSunday = false,
        activeOnMonday = false,
        activeOnTuesday = false,
        activeOnWednesday = false,
        activeOnThursday = false,
        activeOnFriday = false,
        activeOnSaturday = false,
        latitude = 35.6817882863765,
        longitude = 139.76703598784582,
        isActive = true,
        isAlreadyDone = false
    )

    private const val COUNT = 3

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createAlarmData(i))
        }
    }
    fun getData(id: Int): AlarmData? {
        val data = ITEMS.elementAtOrNull(id - 1)
        data?.id = id - 1
        return data
    }

    fun makeData(){
        val data = createAlarmData(ITEMS.size + 1)
        addItem(data)
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
        return DEFAULT_DATA.copy()
    }


}