package com.akiso.gps_alarm

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class AlarmData(
    var id: Int,
    var activeTimeStart: LocalTime,
    var activeTimeEnd: LocalTime,
    var activeDay: MutableList<Int>,
    var location: LatLng
){
    override fun toString(): String = id.toString()
}
