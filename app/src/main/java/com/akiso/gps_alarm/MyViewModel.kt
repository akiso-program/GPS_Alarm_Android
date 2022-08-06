package com.akiso.gps_alarm

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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

    fun getAll(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                data.postValue(dao.getAll())
            }
        }
    }
    fun getById(id:Long):AlarmData?{
        data.value?.forEach {
            if(it.id == id)return it
        }
        return null
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
                val lst = this@MyViewModel.data.value?.toMutableList()
                lst?.remove(data)
                this@MyViewModel.data.postValue(lst)
            }
        }
    }
    fun insert(data:AlarmData):LiveData<Long>{
        val result = MutableLiveData<Long>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                result.postValue(dao.insert(data))
                val lst = this@MyViewModel.data.value?.toMutableList()
                lst?.add(data)
                this@MyViewModel.data.postValue(lst)
            }
        }
        return result
    }

    fun getActiveDataDay(calendar: Calendar):LiveData<List<AlarmData>>{
        val result = MutableLiveData<List<AlarmData>>()
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val data = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> dao.getActiveDataOnSunday()
                    Calendar.MONDAY -> dao.getActiveDataOnMonday()
                    Calendar.TUESDAY -> dao.getActiveDataOnTuesday()
                    Calendar.WEDNESDAY -> dao.getActiveDataOnWednesday()
                    Calendar.THURSDAY -> dao.getActiveDataOnThursday()
                    Calendar.FRIDAY -> dao.getActiveDataOnFriday()
                    Calendar.SATURDAY -> dao.getActiveDataOnSaturday()
                    else -> listOf()
                }
                result.postValue(data)
            }
        }
        return result
    }
}