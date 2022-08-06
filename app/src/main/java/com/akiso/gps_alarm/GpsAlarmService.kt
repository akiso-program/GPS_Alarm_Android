package com.akiso.gps_alarm

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import java.sql.Time
import java.time.LocalTime
import java.util.*

class GpsAlarmStartService : Service(){

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceIntent = Intent(baseContext , GpsAlarmService::class.java)
        baseContext.startForegroundService(serviceIntent)
        return START_REDELIVER_INTENT
    }
}


class MainServiceStartService : Service() {
    private val db = AppDatabase.getInstance(context = this)
    private val dao = db.alarmDao()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "通知のタイトル的情報を設定"
        val id = "casareal_foreground"
        val notifyDescription = "この通知の詳細情報を設定します"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_MIN)
                .apply {
                    description = notifyDescription
                    setSound(null,null)
                }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            setContentTitle("ApsAlarm")
            setContentText("今日のアラーム設定中...")
            setSmallIcon(R.drawable.ic_launcher_background)
        }.build()

        Thread {
            val alarmData = getActiveDataDay(Calendar.getInstance())
            for (data in alarmData) {
                data.isAlreadyDone = false
                dao.update(data)
            }
            val activeData =getActiveData(alarmData)
            if(activeData.isNotEmpty()){
                // startMainServiceNow
                val serviceIntent = Intent(baseContext, GpsAlarmStartService::class.java)
                val pendingIntent = PendingIntent.getActivity(baseContext,0,serviceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
                    val alarm = baseContext.getSystemService(ALARM_SERVICE) as AlarmManager?
                    alarm?.setExact(AlarmManager.RTC_WAKEUP,Calendar.getInstance().timeInMillis, pendingIntent)
            }else{
                val nextData = getNextStart(alarmData)
                if(nextData != null){
                    // startMainService(nextData)
                    val serviceIntent = Intent(baseContext, GpsAlarmStartService::class.java)
                    val pendingIntent = PendingIntent.getActivity(baseContext,0,serviceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                    val alarm = baseContext.getSystemService(ALARM_SERVICE) as AlarmManager?
                    val calendar: Calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, nextData.startToCalendar().get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, nextData.startToCalendar().get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0) //0秒
                        set(Calendar.MILLISECOND, 0) //カンマ0秒
                    }
                    alarm?.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, pendingIntent)
                }
            }

            // MainServiceStartService next 00:00
            val serviceIntent = Intent(baseContext, MainServiceStartService::class.java)
            val pendingIntent = PendingIntent.getActivity(baseContext,0,serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
            val alarm = baseContext.getSystemService(ALARM_SERVICE) as AlarmManager?
            val calendar: Calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0) //0秒
                set(Calendar.MILLISECOND, 0) //カンマ0秒
            }
            alarm?.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, pendingIntent)
            stopForeground(STOP_FOREGROUND_REMOVE)
        }.start()

        startForeground(1, notification)

        return START_REDELIVER_INTENT
    }

    private fun getNextStart(data:List<AlarmData>):AlarmData?{
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val time = Time.valueOf(LocalTime.of(hour,minute).toString())
        return data.filter { it.activeTimeStart > time }.minByOrNull { it.activeTimeStart }
    }
    private fun getActiveData(data:List<AlarmData>):List<AlarmData>{
        val calendar = Calendar.getInstance()
        // 曜日が一致し、start <= time <= end || stat == end
        return data.filter {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val time = Time.valueOf(LocalTime.of(hour,minute).toString())
            (it.activeTimeStart <= time && time <= it.activeTimeEnd) || (it.activeTimeStart == it.activeTimeEnd)
                    && !it.isAlreadyDone
        }
    }

    private fun getActiveDataDay(calendar: Calendar):List<AlarmData>{
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
}

class GpsAlarmService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = AppDatabase.getInstance(context = this)
    private val dao = db.alarmDao()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "通知のタイトル的情報を設定"
        val id = "casareal_foreground"
        val notifyDescription = "この通知の詳細情報を設定します"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_MIN)
                .apply {
                    description = notifyDescription
                    setSound(null,null)
                }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            setContentTitle("通知のタイトル")
            setContentText("通知の内容")
            setSmallIcon(R.drawable.ic_launcher_background)
        }.build()

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_REDELIVER_INTENT
        }
        var allDone = false
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    val data = getActiveData(Calendar.getInstance()).filter { !it.isAlreadyDone }
                    if(data.isEmpty()){
                        fusedLocationClient.removeLocationUpdates(this)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        val nextData = getNextStart(getActiveData(Calendar.getInstance()))
                        nextData?.also {
                            // startMainService(nextData)
                            val serviceIntent = Intent(baseContext, GpsAlarmStartService::class.java)
                            val pendingIntent = PendingIntent.getActivity(baseContext,0,serviceIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                            val alarm = baseContext.getSystemService(ALARM_SERVICE) as AlarmManager?
                            val calendar: Calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, nextData.startToCalendar().get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, nextData.startToCalendar().get(Calendar.MINUTE))
                                set(Calendar.SECOND, 0) //0秒
                                set(Calendar.MILLISECOND, 0) //カンマ0秒
                            }
                            alarm?.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, pendingIntent)
                        }
                    }else{
                        data.forEach {
                            val targetLocation = Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                            val distance = location.distanceTo(targetLocation)
                            if(distance < 100){
                                //alarm
                                Log.d(this.javaClass.name, "${location.latitude} , ${location.longitude}")
                            }
                        }
                    }
                }
            }
        }

        Thread {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                },
                locationCallback,
                null
            )
            (0..100).map {
                Thread.sleep(1000)
            }

        }.start()

        startForeground(1, notification)

        return START_REDELIVER_INTENT
    }

    private fun getActiveDataDay(calendar: Calendar):List<AlarmData>{
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
    private fun getNextStart(data:List<AlarmData>):AlarmData?{
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val time = Time.valueOf(LocalTime.of(hour,minute).toString())
        return data.filter { it.activeTimeStart > time }.minByOrNull { it.activeTimeStart }
    }
    private fun getActiveData(calendar: Calendar):List<AlarmData>{

            // 曜日が一致し、start <= time <= end || stat == end
        return getActiveDataDay(calendar).filter {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val time = Time.valueOf(LocalTime.of(hour,minute).toString())
            (it.activeTimeStart <= time && time <= it.activeTimeEnd) || (it.activeTimeStart == it.activeTimeEnd)
                    && !it.isAlreadyDone
        }
    }
}