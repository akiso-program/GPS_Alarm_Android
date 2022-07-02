package com.akiso.gps_alarm

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class AlarmData(
    val id: Int,
    val activeTimeStart: LocalTime,
    val activeTimeEnd: LocalTime,
    val activeDay: List<Int>,
    val location: LatLng
){
    override fun toString(): String = id.toString()
}
