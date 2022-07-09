package com.akiso.gps_alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.akiso.gps_alarm.placeholder.PlaceholderContent
import com.google.android.gms.location.*

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

class GpsAlarmService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    PlaceholderContent
                        .getActiveData()
                        .filter { !it.isAlreadyDone }
                        .forEach {
                            val targetLocation = Location("").apply {
                                latitude = it.location.latitude
                                longitude = it.location.longitude
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

        Thread(
            Runnable {
                fusedLocationClient.requestLocationUpdates(
                    LocationRequest.create().apply {
                        interval = 10000
                        fastestInterval = 5000
                        priority =  Priority.PRIORITY_HIGH_ACCURACY
                    },
                    locationCallback,
                    null
                )
                (0..100).map {
                    Thread.sleep(1000)
                }
                fusedLocationClient.removeLocationUpdates(locationCallback)
                stopForeground(STOP_FOREGROUND_REMOVE)

            }).start()

        startForeground(1, notification)

        return START_REDELIVER_INTENT
    }
}