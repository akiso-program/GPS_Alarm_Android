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
    var location: LatLng,
    var isActive: Boolean,
    var isAlreadyDone: Boolean
){
    override fun toString(): String = id.toString()
    fun isActive(time: LocalTime): Boolean{
        return isActive && ( activeTimeStart < time && time < activeTimeEnd )
    }
    fun isActive(day:Int):Boolean{
        return isActive && activeDay.contains(day)
    }
}
