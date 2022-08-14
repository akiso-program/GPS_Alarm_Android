package com.akiso.gps_alarm

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.sql.Time
import java.time.LocalTime
import java.util.*


class GpsAlarmResetService : LifecycleService(){
    private val db = AppDatabase.getInstance(context = this)
    private val dao = db.alarmDao()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceIntent = Intent(baseContext , GpsAlarmService::class.java)
        baseContext.startForegroundService(serviceIntent)

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
            stopForeground(STOP_FOREGROUND_REMOVE)
        }.start()

        setNextResetService()

        startForeground(1, notification)

        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT
    }

    private fun setNextResetService(){
        val serviceIntent = Intent(this, GpsAlarmService::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0) //0秒
            set(Calendar.MILLISECOND, 0) //カンマ0秒
            add(Calendar.DAY_OF_WEEK, 1)
        }
        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager?
        alarm?.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis, pendingIntent)
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

class GpsAlarmService : LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private lateinit var dao: AlarmDao

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "GPS Alarm"
        val id = "casareal_foreground"
        val notifyDescription = "この通知の詳細情報を設定します"

        db = AppDatabase.getInstance(context = applicationContext)
        dao = db.alarmDao()

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                CoroutineScope(Dispatchers.Main).launch {
                    for (location in p0.locations){
                        getActiveData(Calendar.getInstance()).observe(this@GpsAlarmService){
                            it.forEach {
                                val targetLocation = Location("").apply {
                                    latitude = it.latitude
                                    longitude = it.longitude
                                }
                                val distance = location.distanceTo(targetLocation)
                                if(distance < 100){
                                    //alarm
                                    Log.d(this.javaClass.name, "${location.latitude} , ${location.longitude}")
                                    val pendingIntent = PendingIntent.getActivity(
                                        application,
                                        777,
                                        Intent(application, AlarmActivity::class.java),
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                    val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
                                    val nextWakeupTime = System.currentTimeMillis() + 500
                                    alarmManager.setAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        nextWakeupTime,
                                        pendingIntent
                                    )
                                    it.isAlreadyDone = true
                                    setData(it)
                                    //pendingIntent.send()
                                }
                            }
                        }
                    }
                }
            }
        }
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Thread{

            (0..100).map {
                Thread.sleep(100)
            }

            val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager?
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 500,
                PendingIntent.getActivity(
                    application,
                    777,
                    Intent(application, AlarmActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }.start()

        startForeground(1, notification)

        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT
    }

    private suspend fun getActiveDataDay(calendar: Calendar): LiveData<List<AlarmData>> {
        val result = MutableLiveData<List<AlarmData>>()
        withContext(Dispatchers.IO) {
            val data = when (calendar.get(Calendar.DAY_OF_WEEK)) {
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
        return result
    }
    private suspend fun getActiveData(calendar: Calendar):LiveData<List<AlarmData>>{
        val result = MutableLiveData<List<AlarmData>>()
        // 曜日が一致し、start <= time <= end || stat == end
        withContext(Dispatchers.Main) {
            getActiveDataDay(calendar).observe(this@GpsAlarmService) {
                val active = it.filter { data ->
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    ((data.activeTimeStart.hours <= hour && hour <= data.activeTimeEnd.hours)
                            || (data.activeTimeStart.minutes <= minute && minute <= data.activeTimeEnd.minutes)
                            || (data.activeTimeStart == data.activeTimeEnd))
                            && !data.isAlreadyDone
                }
                result.postValue(active)
            }
        }
        return result
    }
    private fun setData(data:AlarmData){
        dao.update(data)
    }
}