package com.akiso.gps_alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Time
import java.time.LocalTime
import java.util.*

class MyViewModel (application: Application):
    AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(context = application)
    private val dao = db.alarmDao()

    val data = MutableLiveData<List<AlarmData>>()
    fun getAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                data.value = dao.getAll()
            }
        }
    }
    fun getById(id:Int):LiveData<AlarmData?>{
        val result = MutableLiveData<AlarmData?>()
        viewModelScope.launch {
            val data = dao.loadById(id)
            result.postValue(data)
        }
        return result
    }
    fun update(data:AlarmData){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.update(data)
            }
        }
    }
    fun delete(data:AlarmData){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(data)
            }
        }
    }
    fun insert(data:AlarmData){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertAll(data)
            }
        }
    }

    fun newData():LiveData<AlarmData>{
        val result = MutableLiveData<AlarmData>()
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val newData = AlarmData()
                insert(newData)
                result.postValue(newData)
            }
        }
        return result
    }

    fun getActiveDataDay(calendar: Calendar):List<AlarmData>{
        return when(calendar.get(Calendar.DAY_OF_WEEK)){
            Calendar.SUNDAY -> dao.getActiveDataOnSunday()
            Calendar.MONDAY -> dao.getActiveDataOnMonday()
            Calendar.TUESDAY -> dao.getActiveDataOnTuesday()
            Calendar.WEDNESDAY -> dao.getActiveDataOnWednesday()
            Calendar.THURSDAY -> dao.getActiveDataOnThursday()
            Calendar.FRIDAY -> dao.getActiveDataOnFriday()
            Calendar.SATURDAY -> dao.getActiveDataOnSaturday()
            else -> listOf()
        }
    }

    fun getNextStart(calendar: Calendar):AlarmData?{
        for(i in 0..6) {
            calendar.add(Calendar.DAY_OF_WEEK,1)
            val data = getActiveDataDay(calendar)
            if(data.isNotEmpty())return data.firstOrNull()
        }
        return null
    }

    fun getActiveData(calendar: Calendar):List<AlarmData>{
        // 曜日が一致し、start <= time <= end || stat == end
        return getActiveDataDay(calendar).filter {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val time = Time.valueOf(LocalTime.of(hour,minute).toString())
            (it.activeTimeStart <= time && time <= it.activeTimeEnd) || (it.activeTimeStart == it.activeTimeEnd)
        }
    }
}