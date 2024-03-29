package com.akiso.gps_alarm

import android.content.Context
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import java.sql.Time
import java.util.*

@Entity
data class AlarmData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "active_time_start") var activeTimeStart: Time,
    @ColumnInfo(name = "active_time_end") var activeTimeEnd: Time,
    @ColumnInfo(name = "active_on_sunday") var activeOnSunday: Boolean,
    @ColumnInfo(name = "active_on_monday") var activeOnMonday: Boolean,
    @ColumnInfo(name = "active_on_tuesday") var activeOnTuesday: Boolean,
    @ColumnInfo(name = "active_on_wednesday") var activeOnWednesday: Boolean,
    @ColumnInfo(name = "active_on_thursday") var activeOnThursday: Boolean,
    @ColumnInfo(name = "active_on_friday") var activeOnFriday: Boolean,
    @ColumnInfo(name = "active_on_saturday") var activeOnSaturday: Boolean,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "is_active") var isActive: Boolean,
    @ColumnInfo(name = "is_already_done") var isAlreadyDone: Boolean,
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
    fun getLocation():LatLng{
        return LatLng(latitude,longitude)
    }
    fun setLocation(latLng: LatLng){
        latitude = latLng.latitude
        longitude = latLng.longitude
    }
}

@Dao
interface AlarmDao{

    @Query("SELECT * FROM alarmData")
    fun getAll(): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE id IN (:ids)")
    fun loadAllByIds(ids: LongArray): List<AlarmData>

    @Query("SELECT * FROM alarmData WHERE id IS (:id)")
    fun loadById(id: Long): AlarmData?

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

    @Insert
    fun insertAll(vararg data: AlarmData):List<Long>

    @Insert
    fun insert(data: AlarmData):Long

    @Update
    fun update(data: AlarmData)

    @Delete
    fun delete(data: AlarmData)
}

@Database(entities = [AlarmData::class], version = 1, exportSchema = false)
@TypeConverters(TimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "alarm_list.db"
                ).build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

class TimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Time? {
        return if (value == null) null else Time(value)
    }

    @TypeConverter
    fun toTimestamp(time: Time?): Long? {
        return time?.toInstant()?.toEpochMilli()
    }
}
