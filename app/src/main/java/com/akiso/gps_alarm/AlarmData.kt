package com.akiso.gps_alarm

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import androidx.room.Entity
import androidx.room.Query
import com.google.android.gms.maps.model.LatLng
import java.sql.Time
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@Entity
data class AlarmData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo (name = "active_time_start") var activeTimeStart: Time = Time.valueOf("00:00"),
    @ColumnInfo(name = "active_time_end") var activeTimeEnd: Time = Time.valueOf("00:00"),
    @ColumnInfo(name = "active_on_sunday") var activeOnSunday: Boolean = true,
    @ColumnInfo(name = "active_on_monday") var activeOnMonday: Boolean= true,
    @ColumnInfo(name = "active_on_tuesday") var activeOnTuesday: Boolean= true,
    @ColumnInfo(name = "active_on_wednesday") var activeOnWednesday: Boolean= true,
    @ColumnInfo(name = "active_on_thursday") var activeOnThursday: Boolean= true,
    @ColumnInfo(name = "active_on_friday") var activeOnFriday: Boolean= true,
    @ColumnInfo(name = "active_on_saturday") var activeOnSaturday: Boolean= true,
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0,
    @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
    @ColumnInfo(name = "is_active") var isActive: Boolean = true,
    @ColumnInfo(name = "is_already_done") var isAlreadyDone: Boolean = false
){
    override fun toString(): String = id.toString()
    fun startToCalendar():Calendar{
        return Calendar.Builder().apply {
            setInstant(activeTimeStart.time)
        }.build()
    }
    fun endToCalendar():Calendar{
        return Calendar.Builder().apply {
            setInstant(activeTimeEnd.time)
        }.build()
    }
    var location: LatLng = LatLng(latitude,longitude)
    set(value){
        latitude = value.latitude
        longitude = value.longitude
        field = value
    }
}

@Dao
interface AlarmDao{

    @Query("SELECT * FROM alarmData")
    fun getAll(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE id IS (:id)")
    fun loadById(id: Int): AlarmData?

    @Query("SELECT * FROM alarmData WHERE active_on_sunday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnSunday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_monday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnMonday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_tuesday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnTuesday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_wednesday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnWednesday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_thursday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnThursday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_friday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnFriday(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE active_on_saturday IS 1 AND is_active IS 1 ORDER BY active_time_start")
    fun getActiveDataOnSaturday(): List<AlarmData>

    @Query("SELECT last_insert_id() FROM alarmData")
    fun getLastInsertId(): Int

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

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "yourdb.db").build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
