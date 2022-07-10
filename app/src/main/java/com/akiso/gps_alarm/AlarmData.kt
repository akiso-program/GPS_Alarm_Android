package com.akiso.gps_alarm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import androidx.room.Entity
import androidx.room.Query
import java.sql.Time
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@Entity
data class AlarmData(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "active_time_start") var activeTimeStart: Time,
    @ColumnInfo(name = "active_time_end") var activeTimeEnd: Time,
    @ColumnInfo(name = "active_on_sunday") var activeOnSunday: Boolean,
    @ColumnInfo(name = "active_on_monday") var activeOnMonday: Boolean,
    @ColumnInfo(name = "active_on_tuesday") var activeOnTuesday: Boolean,
    @ColumnInfo(name = "active_on_wednesday") var activeOnWednesday: Boolean,
    @ColumnInfo(name = "active_on_thursday") var activeOnThursday: Boolean,
    @ColumnInfo(name = "active_on_friday") var activeOnFriday: Boolean,
    @ColumnInfo(name = "active_on_saturday") var activeOnSaturday: Boolean,
    @ColumnInfo(name = "latitude") var latitude: Long,
    @ColumnInfo(name = "longitude") var longitude: Long,
    @ColumnInfo(name = "is_active") var isActive: Boolean,
    @ColumnInfo(name = "is_already_done") var isAlreadyDone: Boolean
){
    override fun toString(): String = id.toString()
    fun startToLCalendar():Calendar{
        return Calendar.Builder().apply {
            setInstant(activeTimeStart.time)
        }.build()
    }
    fun endToCalendar():Calendar{
        return Calendar.Builder().apply {
            setInstant(activeTimeEnd.time)
        }.build()
    }
}

@Dao
interface AlarmDao{

    @Query("SELECT * FROM alarmData")
    fun getAll(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_sunday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnSunday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_monday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnMonday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_tuesday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnTuesday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_wednesday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnWednesday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_thursday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnThursday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_friday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnFriday(time: Time): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_saturday IS 1 AND active_time_start < (:time)")
    fun getActiveDataOnSaturday(time: Time): List<AlarmData>

    @Insert
    fun insertAll(vararg data: AlarmData)

    @Update
    fun update(data: AlarmData)

    @Delete
    fun delete(data: AlarmData)
}

@Database(entities = [AlarmData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
